# Autostereogram-Creator


An autostereogram is an image that creates the illusion of a 3d object when viewed properly. Each individual eye should be resolved on the image, but oriented towards some position behind or in front of the image, rather than the image itself.
Random dot stereograms commonly crop up but have some disadvantages. They are problematic to alias well which leads to a rough-grained 3d object. They are also harder to latch on to due to a lack of an recognizable pattern.

The autostereograms I created took ~12 seconds to generate. The closer heightmap[][] values on average are to 0, the more computations have to be made so if build() is unacceptably slow consider changing this.


There are four functions available for the Autostereogram class:

Autostereogram(int windowx, int windowy, int observerdistance, int eyedistance, StartFunction sf, ColorScheme cs)

build(float[][] heightmap, int seed, String filename)

applyColorScheme(ColorScheme cs, String filename)

verifyBorder(float[][] heightmap)


The first four arguments to Autostereogram() are windowx, windowy, observerdistance, eyedistance and are all measured in pixels. winx and winy describe the dimensions of the image file that will be produced. observerdistance is how far away a person should be to view the image correctly and eyedistance is the distance between their eyes.

There are two interfaces: one (StartFunction) that takes in a point in 3d space and returns a floating point number, and one (ColorScheme) that takes in a real number and returns a integer that describes a color as in the default sRGB ColorModel (Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue). See Color.getRGB(). The first interface also has a mechanism makeNew to be randomized/refreshed with an integer seed but this function can be left empty with no consequence. 

cs can be left as null, in which case the image will be drawn in greyscale.

The first four arguments to Autostereogram() should obviously be positive and an IllegalArgumentException is thrown if any of them are not.


float heightmap[][] represents the surface situated in 3D space to be visualized. The "window" is a rectangle representing the eventual image file which lies at y=0 in the same coordinate space as heightmap[][].

class BadHeightMapException exists if an unusable heightmap is passed. It contains an enum that provides additional information on the reason for the exception being thrown.

The sign of all values of heightmap[][] must be the same throughout, and no value in heightmap[][] should be 0. If not a BadHeightMapException with reason NOTNONZERO will be thrown. eyedistance should be greater than every value in heightmap[][]. If not a BadHeightMapException with reason TOOHIGH will be thrown.
If heightmap[][] contains positive values, then it means the observer is to be cross-eyed, and negative values: wall-eyed.

heightmap[][] must be large enough that for any point in the window, a line passing through that point and either eye must intersect the surface. For instance, if heightmap[][] contains only negative values, it must necessarily have dimensions greater than (winx, winy) because lines drawn from the top left corner to either eye will not intersect the surface. If this is found to not be the case a BadHeightMapException will be thrown, with reason TOOSMALL. heightmap[][] is centered at the center of the window in any case.

The surface in 3D should also be fully visible from the perspective of both eyes, i.e. any line passing through an eye must not intersect the surface twice. This is not checked for (too computationally intensive for its worth) and although build() will return the resulting image will have noticeable defects.

Calling verifyBorder() will determine if the provided heightmap[][] satisfies the property described two paragraphs ago. If a point is found such that no line exists passing through an eye and that point, which also intersects the surface, the coordinates of that point are returned. Otherwise null is returned.


build() will build an autostereogram with the desired parameters specified on construction.

applyColorScheme will color the stored output from an earlier build() call and write it to the specified filename. 

Concerning both build() and applyColorScheme, the image will be stored in a file given by filename, in .png format. If filename already exists, it is overwritten. If no filename is provided (filename is null) an IllegalArgumentException is thrown. An IOException is thrown if some error happens when writing to the file.
