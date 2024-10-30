# Autostereogram-Creator


An autostereogram is an image that creates the illusion of a 3d object when viewed properly. Each individual eye should be resolved on the image, but oriented towards some position behind or in front of the image, rather than the image itself.
Random dot stereograms commonly crop up but have some disadvantages. They are problematic to alias well which leads to a rough-grained 3d object. They are also harder to latch on to due to a lack of an recognizable pattern. The "continuous" autostereograms that this project produces solve both of these problems. You need to be about ~30cm away to view each of the two sample images.

The autostereograms I created took ~12 seconds to generate. The closer heightmap[][] values on average are to 0, the more computations have to be made so if build() is unacceptably slow consider changing this.
