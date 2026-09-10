
window.addEventListener("load", (event) => {
	const fragment = window.location.hash.substring(1);

	if (fragment.length != 0) {
		try {
			const json = JSON.parse(atob(fragment));

			if (json.mode == "import") {
				fetchShader(json.source, json.id, editorImport);
				return;
			}

			if (json.mode == "show") {
				editorImport(json);
				return;
			}

			editorError("Unknown command, try reloading the page");
		} catch(err) {
			editorError("Failed to decode command");
		}
	}
});

function shaderURLtoObject(url) {

	// shadertoy/view
	const shadertoy_view = /^.*shadertoy\.com\/view\/([a-zA-Z0-9]+)$/g;
	for (let match of url.matchAll(shadertoy_view)) {
		return {source: "shadertoy", id: match[1]};
	}

	return null;
}

function importExternalShader(json) {
	json.mode = "import";
	window.location.replace("./editor.html#" + btoa(JSON.stringify(json)));
}

function importExternalShaderFromURL() {
	const json = shaderURLtoObject(document.getElementById("import_url").value);

	if (json == null) {
		editorError("Failed to decode URL, is the side you linked supported?");
		return;
	}

	importExternalShader(json);
}

// editor functions

var editor_timeout_id = null;

function editorError(message) {
	const error = document.getElementById("editor_error");

	error.innerText = message;
	error.style.opacity = 1;

	if (editor_timeout_id != null) {
		clearTimeout(editor_timeout_id);
	}

	editor_timeout_id = setTimeout(() => {
		error.style.opacity = 0;
	}, 4000);
}

function editorExport() {
	const fragment = document.getElementById("editor_fragment").value;
	const name = document.getElementById("editor_name").value;
	const format = parseInt(document.getElementById("editor_format").value);
	const description = document.getElementById("editor_description").value;

	if (name.length < 1) {
		editorError("Please specify a name for your shader pack");
		return;
	}

	if (!/^([a-zA-Z0-9 _\-.]+)$/.test(name)) {
		editorError("Please don't use special characters in the pack name");
		return;
	}

	const metadata = makePackMeta(format, description);
	makeShaderPack(name, fragment, null, metadata);
}

function editorImport(shader) {
	document.getElementById("editor_fragment").value = shader.code;
	document.getElementById("editor_name").value = shader.name;
	document.getElementById("editor_description").value = shader.description;
}

// import functions

function fetchShader(source, id, callback) {

	// don't look :gun:
	const key = atob("TmRybGhO");

	if (source == "shadertoy") {
		fetch(`https://www.shadertoy.com/api/v1/shaders/${id}?key=${key}`).then(res => res.json()).then(json => {
			const shader = json.Shader;

			if (shader == null) {
				editorError(`Failed to fetch shader from the API`);
				return;
			}

			let passes = [];

			for (const pass of shader.renderpass) {
				if (pass.type == "image") {
					passes.push(pass);
				}
			}

			if (passes.length > 1) {
				editorError(`Incompatible shader, more than one render pass is being used`);
				return;
			}

			const converted = shadertoyConvert(passes[0].code);

			if (converted != null) {
				callback({
					code: converted,
					name: shader.info.name,
					author: shader.info.username,
					description: shader.info.description
				});
			}
		});
		return;
	}

	editorError(`Unknown shader source '${source}'`);
}


// conversion functions

function findUniqueName(code, name) {
	let unique = name;
	let index = 0;

	if (code.includes(unique)) {
		unique += "_glslmc_";
	} else {
		return unique;
	}

	while (code.includes(unique + index)) {
		index ++;
	}

	return unique + index;
}

function renameSymbol(code, name) {
	const unique = findUniqueName(code, name);
	if (name == unique) return code;
	return code.replaceAll(name, unique);
}

function shadertoyConvert(code) {

	let uniforms = "";
	let unsuported = [];

	// TODO: handle VR main

	/*
		uniform vec3      iResolution;           // viewport resolution (in pixels)
		uniform float     iTime;                 // shader playback time (in seconds)
		uniform float     iTimeDelta;            // render time (in seconds)
		uniform float     iFrameRate;            // shader frame rate
		uniform int       iFrame;                // shader playback frame
		uniform float     iChannelTime[4];       // channel playback time (in seconds)
		uniform vec3      iChannelResolution[4]; // channel resolution (in pixels)
		uniform vec4      iMouse;                // mouse pixel coords. xy: current (if MLB down), zw: click
		uniform samplerXX iChannel0..3;          // input channel. XX = 2D/Cube
		uniform vec4      iDate;                 // (year, month, day, time in seconds)
		uniform float     iSampleRate;           // sound sample rate (i.e., 44100)
	*/

	// check for unsuported uniform usage
	if (code.includes("iChannelResolution")) unsuported.push("iChannelResolution");
	if (code.includes("iChannel1")) unsuported.push("iChannel1");
	if (code.includes("iChannel2")) unsuported.push("iChannel2");
	if (code.includes("iChannel3")) unsuported.push("iChannel3");
	if (code.includes("iChannelTime")) unsuported.push("iChannelTime");
	if (code.includes("iFrame")) unsuported.push("iFrame"); // TODO implement
	if (code.includes("iTimeDelta")) unsuported.push("iTimeDelta");

	if (unsuported.length != 0) {
		editorError("This shader uses some unsuported uniforms: " + unsuported.join(", "));
		return null;
	}

	// epic workarounds
	code = code.replaceAll("iSampleRate", "44100"); // this should never be in an image shader but whatever, let's patch it too
	code = code.replaceAll("iDate", "vec4(2023, 8, 9, 1000)"); // stupid but it will make it compile

	// start translating
	if (code.includes("iTime")) {
		code = renameSymbol(code, "time").replaceAll("iTime", "time");
		uniforms += "uniform float time;\n";
	}

	if (code.includes("iMouse")) {
		code = renameSymbol(code, "mouse").replaceAll("iMouse", "mouse.xyxy");
		uniforms += "uniform vec2 mouse;\n";
	}

	if (code.includes("iResolution")) {
		code = renameSymbol(code, "resolution").replaceAll("iResolution", "resolution.xyx");
		uniforms += "uniform vec2 resolution;\n";
	}

	if (code.includes("iChannel0")) {
		code = renameSymbol(code, "image").replaceAll("iChannel0", "image");
		uniforms += "uniform sampler2D image;\n";
	}

	// time to unfuck the entrypoint
	const entrypoint = /void +mainImage *\( *out +vec4 +(\w+) *, *in +vec2 +(\w+) *\)/g;

	// there can only be one, i hope
	const args = code.matchAll(entrypoint).next().value;
	uniforms += "out vec4 " + args[1] + ";\n"; // color out

	code = code.replace(entrypoint, "void main()");
	code = code.replaceAll(args[2], "gl_FragCoord.xy");

	return uniforms + code;

}

// common functions

function makePackMeta(format, description) {
	return {pack: {pack_format: parseInt(format), description: description}}
}

function makeShaderPack(name, fragment, vertex, metadata) {
	const pack = new JSZip();

	// minecraft resource pack file
	pack.file("pack.mcmeta", JSON.stringify(metadata));

	// GLSL Panorma shader resource path
	const panorama = pack.folder("assets").folder("glsl_panorama").folder("panorama");

	// add shader files
	if (fragment != null) panorama.file("shader.frag", fragment);
	if (vertex != null) panorama.file("shader.vert", vertex);

	pack.generateAsync({type:"blob"}).then(content => {
		saveAs(content, `${name}.zip`);
	});
}
