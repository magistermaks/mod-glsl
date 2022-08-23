## Documentation
This mod loads the panorama shaders from the resource pack, so to create a Panorama shader start
by creating a simple, valid resourcepack.

#### Creating a Resourcepack
Learn more here: [Minecraft Wiki - Resource Packs](https://minecraft.fandom.com/wiki/Resource_Pack#Folder_structure]),
for example how to add a custom icon to your pack.

- Create a directory, the name of that directory will be the name of your pack
- Create a file named `pack.mcmeta` inside, make sure the extension is correct!
- Inside that file put the following JSON object: (without the comment!)
```json5
{
    // use 6 for 1.16.2-1.16.5, 7 for 1.17.x, 8 for 1.18.x, 9 for 1.19.x
    "pack": {
        "pack_format": 7,
        "description": "My epic panorama shader pack"
    }
}
```
Congrats you now have a valid resourcepack! If you drop that directory into the `resourcepacks` 
folder it will show up in-game.

#### Panorama Shader Structure
Inside your resourcepack folder create a directory named `assets`, 
and `glsl_panorama` directory inside that, then `panorama` inside that one.  
All the files of your shader will be located here, the allowed files are:
- `shader.vert` the vertex shader, in 99% of the cases you don't want to use that one, so we will ignore it
- `shader.frag` the fragment shader, this will be your GLSL shader
- `texture.png` an optional texture that will be loaded for your shader if provided

#### Fragment Shader
Your shader shouldn't contain the `#version XXX` directive, it will be added during loading.
The color of the output fragment should be defined by providing an output vector and writing to it,
but if you use the outdated `gl_FragColor` it will get converted during loading and _should_ also work.

A simple "Hello World" example `shader.frag`, it renders a simple solid green background
```glsl
out vec4 fragment;

void main() {
    fragment = vec4(0, 1, 0, 1);
}
```

#### Provided Uniforms
The following uniforms are available to your shaders
- Uniform `time` (`float`) number of second the shader has been playing for.
- Uniform `mouse` (`vec2`) the position of the mouse, with top-left being (0,0) and bottom-right being (1,1).
- Uniform `resolution` (`vec2`) this value should be treated as the window dimensions. 
- Uniform `image` (`sampler2D`) texture sampler, only provided if the `texture.png` file is provided, the texture is set to repeat.  

You can also use `gl_FragCoord.xy` to access the position (window-space) of the fragment that is being processed.

Example shader, draws a circle around the cursor:
```glsl
uniform vec2 mouse;
uniform vec2 resolution;

out vec4 fragment;

void main() {
    // remap to screen-space (with aspect-ratio correction)
    vec2 pos = (gl_FragCoord.xy - resolution) / resolution.y;

    // invert, offset, and distort to account for the aspect-ratio
    vec2 cursor = (vec2(mouse.x - 1, 1 - mouse.y - 1) * resolution) / resolution.y;

    // get distance to mouse
    float dist = distance(cursor, pos);

    // make a circle around the mouse
    float green = (dist < 0.1) ? 1 : 0;

    // set the fragment color
    fragment = vec4(0, green, 0, 1);
}
```

#### Vertex Shaders
Again, you probably won't need them, but for the sake of completenss: 
- Only one layout location is provided `layout (location=0) vec3 pos;` 
- The shader is executed on a buffer containing 4 vertices, each for one screen corner.
