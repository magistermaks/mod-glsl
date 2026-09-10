
window.addEventListener("load", galleryPopulate);

var shaders = [
	"https://www.shadertoy.com/view/mlS3DV",
	"https://www.shadertoy.com/view/Ms2SD1",
	"https://www.shadertoy.com/view/tlVGDt",
	"https://www.shadertoy.com/view/XtlSD7"
];

function galleryView() {
	window.location.href = this.dataset.url;
}

function galleryEdit() {
	importExternalShader(shaderURLtoObject(this.dataset.url));
}

function galleryAppendEntry(gallery, url) {
	const row = document.createElement("tr");
	const json = shaderURLtoObject(url);

	const nodeSite = document.createElement("td");
	nodeSite.innerText = json.source;

	const nodeSlug = document.createElement("td");
	nodeSlug.innerText = json.id;

	const nodeView = document.createElement("td");
	const nodeViewButton = document.createElement("button");
	nodeViewButton.dataset.url = url;
	nodeViewButton.innerText = "View";
	nodeViewButton.addEventListener("click", galleryView);

	nodeView.appendChild(nodeViewButton);

	const nodeEdit = document.createElement("td");
	const nodeEditButton = document.createElement("button");
	nodeEditButton.dataset.url = url;
	nodeEditButton.innerText = "Edit";
	nodeEditButton.addEventListener("click", galleryEdit);

	nodeEdit.appendChild(nodeEditButton);

	row.appendChild(nodeSite);
	row.appendChild(nodeSlug);
	row.appendChild(nodeView);
	row.appendChild(nodeEdit);

	gallery.appendChild(row);
}

function galleryPopulate() {
	const gallery = document.getElementById("gallery");
	gallery.innerHTML = "";

	for (let shader of shaders) {
		galleryAppendEntry(gallery, shader);
	}

}
