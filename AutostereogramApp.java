import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.Random;
import java.lang.Math;


/*
There are four functions available for the Autostereogram class:

Autostereogram(int windowx, int windowy, int observerdistance, int eyedistance, StartFunction sf, ColorScheme cs)
build(float[][] heightmap, int seed, String filename)
applyColorScheme(ColorScheme cs, String filename)
verifyBorder(float[][] heightmap)


The first four arguments to Autostereogram() are windowx, windowy, observerdistance, eyedistance and are all measured 
in pixels. winx and winy describe the dimensions of the image file that will be produced. observerdistance is how far 
away a person should be to view the image correctly and eyedistance is the distance between their eyes.

There are two interfaces: one (StartFunction) that takes in a point in 3d space and returns a floating point number, 
and one (ColorScheme) that takes in a real number and returns a integer that describes a color as in the default sRGB 
ColorModel (Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue). See Color.getRGB(). 
cs can be left as null, in which case the image will be drawn in greyscale.

StartFunction is supposed to provide random *continuous* input. The rate at which colors are cycled through in the 
image is affected by how rapidly a StartFunction varies. For example, replacing return res with return 2.0f*res in 
sf.calculate() makes the image more sharply contrasting. It also has a mechanism makeNew to be randomized/refreshed 
with an integer seed but this function can be left empty with no consequence. 

The first four arguments to Autostereogram() should obviously be positive and an IllegalArgumentException is thrown 
if any of them are not.


float heightmap[][] represents the surface situated in 3D space to be visualized. The "window" is a rectangle 
representing the eventual image file which lies at y=0 in the same coordinate space as heightmap[][].

class BadHeightMapException exists if an unusable heightmap is passed. It contains an enum that provides additional 
information on the reason for the exception being thrown.

The sign of all values of heightmap[][] must be the same throughout, and no value in heightmap[][] should be 0. If not a 
BadHeightMapException with reason NOTNONZERO will be thrown. eyedistance should be greater than every value in 
heightmap[][]. If not a BadHeightMapException with reason TOOHIGH will be thrown.
If heightmap[][] contains positive values, then it means the observer is to be cross-eyed, and negative values: wall-eyed.

heightmap[][] must be large enough that for any point in the window, a line passing through that point and either eye must 
intersect the surface. For instance, if heightmap[][] contains only negative values, it must necessarily have dimensions 
greater than (winx, winy) because lines drawn from the top left corner to either eye will not intersect the surface. If 
this is found to not be the case a BadHeightMapException will be thrown, with reason TOOSMALL. heightmap[][] is centered 
at the center of the window in any case.

The surface in 3D should also be fully visible from the perspective of both eyes, i.e. any line passing through an eye 
must not intersect the surface twice. This is not checked for (too computationally intensive for its worth) and although 
build() will return the resulting image will have noticeable defects.

Calling verifyBorder() will determine if the provided heightmap[][] satisfies the property described two paragraphs ago. 
If a point is found such that no line exists passing through an eye and that point, which also intersects the surface, 
the coordinates of that point are returned. Otherwise null is returned.


build() will build an autostereogram with the desired parameters specified on construction.

applyColorScheme will color the stored output from an earlier build() call and write it to the specified filename. 

Concerning both build() and applyColorScheme, the image will be stored in a file given by filename, in .png format. 
If filename already exists, it is overwritten. If no filename is provided (filename is null) an IllegalArgumentException 
is thrown. An IOException is thrown if some error happens when writing to the file.
*/

public class AutostereogramApp {
    public static void main(String[] args) {
        //for demonstration purposes only
        if (args.length != 2) { System.out.printf("usage: java AutostereogramApp filename seed\n"); return;}
        int seed1;
        try {
            seed1 = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            System.out.printf("Seed must be an integer.\n"); 
            return;
        }

        StartFunction sf = new StartFunction() {
            int funcC = 40; //number of cosines to add
            double[][] cosData = new double[funcC][5]; //a sum of Acos(Bx+Cy+Dz+E)

            public void makeNew(int seed1) {
                Random rand = new Random(seed1);
                for (int i = 0; i<funcC; i++) {
                    cosData[i][0] = rand.nextDouble(0.01, 0.05);
                    cosData[i][1] = rand.nextDouble(0.01, 0.2); //arbitrary.
                    double temp1 = rand.nextDouble(0.01, 0.2);
                    if (rand.nextInt(2) == 0) { 
                        cosData[i][2] = temp1;
                    } else {
                        cosData[i][2] = -temp1;
                    }
                    temp1 = rand.nextDouble(0.01, 0.2);
                    if (rand.nextInt(2) == 0) { 
                        cosData[i][3] = temp1;
                    } else {
                        cosData[i][3] = -temp1;
                    }
                    cosData[i][4] = rand.nextDouble(0, 6.28);
                }
            }

            public float calculate(float x, float y, float z) {
                float res = 0.0f;
                for (int i=0; i<funcC; i++) {
                    res += cosData[i][0]*Math.cos(cosData[i][1]*x+cosData[i][2]*y+cosData[i][3]*z+cosData[i][3]);
                }
                return res;
            }
        };
        int mapx = 1200;
        int mapy = 800;
        float[][] heightmap = new float[mapx][mapy];
        //this makes a square pyramid
        for (int x1=0; x1 < mapx; x1++) {
            for (int y1=0; y1 < mapy; y1++) {
                if (4.0f*x1 < mapx || 4.0f*x1 > 3.0f*mapx || 6.0f*y1 < mapy || 6.0f*y1 > 5.0f*mapy) {
                    heightmap[x1][y1] = 300.0f;
                } else {
                    if (4.0f*mapy*(x1-mapx/4.0f)<3.0f*mapx*(y1-mapy/6.0f)) {
                        //NW quadrants
                        if (4*mapy*(x1-0.75f*mapx)<-3.0f*mapx*(y1-mapy/6.0f)) {
                            //SW quadrants
                            heightmap[x1][y1] = (300.0f + ((x1-mapx/4.0f) * 100.0f) / (mapx / 4.0f));
                        } else {
                          //north
                            heightmap[x1][y1] = (300.0f + (((5.0f*mapy)/6.0f-y1)*100.0f)/(mapy/3.0f));
                        }
                    } else {
                        if (4.0f*mapy*(x1-3.0f*mapx/4.0f)<-3.0f*mapx*(y1-mapy/6.0f)) {
                            heightmap[x1][y1] = (300.0f + ((y1-mapy/6.0f)*100.0f)/(mapy/3.0f));
                        } else {
                            //north
                            heightmap[x1][y1] = (300.0f + (((0.75f*mapx)-x1)*100.0f)/(mapx / 4.0f));
                        }
                    }
                }
            }
        }
        float pixelsize = 0.0162f; //in cm
        int eyedist2 = (int) (6.5f/pixelsize);
        int me = (int) (30.0f/pixelsize);
        Autostereogram s1 = new Autostereogram(1200, 800, eyedist2, me, sf, null);
        try {
            s1.build(heightmap, seed1, args[0]);
        } catch (BadHeightMapException e) {
            if (e.w == BadHeightMapException.which.TOOSMALL) {
                System.out.printf("Heightmap is too small compared to window; point (%d, %d) cannot reach it.", e.x, e.y);
            } else if (e.w == BadHeightMapException.which.NOTNONZERO){
                System.out.printf("All points in heightmap[][] must have the same sign: points (0,0) and (%d, %d) have different sign or one is zero.\n", e.x, e.y);
            } else {
                System.out.printf("All points in heightmap[][] must be lower than the provided eyeline: point (%d,%d) is not.\n", e.x, e.y);
            }
        } catch(IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

interface StartFunction {
    public float calculate(float x, float y, float z);
    public void makeNew(int seed);
}

interface ColorScheme {
    public int giveColor(float v);
}

class BadHeightMapException extends Exception {
    public enum which{TOOSMALL, TOOHIGH, NOTNONZERO}
    public which w;
    public int x;
    public int y;
    public BadHeightMapException(which w1, int x1, int y1) {
        w=w1;
        x=x1;
        y=y1;
    }
}

class Autostereogram {
    private int winx;
    private int winy;
    private int border = 30; //number of pixels in border
    private float[][] heightmap; //describes the 3d surface that should appear
    private float[][] agradient;
    private StartFunction f;
    private int mapx; //width of heightmap
    private int mapy;
    private int eyedist; //distance between eyes in pixels
    private int observerdist; //distance observer is to image in pixels
    private int mapadjustx; //exists to center the window and the heightmap since they can be different sizes. To translate from window to map coordinates, add mapadjust
    private int mapadjusty;
    private BufferedImage b1;
    private enum Direction {LEFT, RIGHT};
    private static ColorScheme greyscale = new ColorScheme() {
        public int giveColor(float v) {int c = Math.min(255, (int) (v*256)); return ((new Color(c, c, c)).getRGB());}
    };
    private ColorScheme cs;
        
    public Autostereogram(int winx1, int winy1, int eyedist1, int observerdist1, StartFunction f1, ColorScheme cs1) {
        if (winx1 <= 0 || winy1 <= 0 || eyedist1 <= 0 || observerdist1 <= 0) {throw new IllegalArgumentException("first four arguments to Autostereogram() must be positive");}
        winx = winx1;
        winy = winy1;
        f = f1;
        eyedist = eyedist1;
        observerdist = observerdist1;
        agradient = new float[winx1][winy1];
        if (cs1 == null) {cs1 = greyscale;}
        cs = cs1;
        b1 = new BufferedImage(winx1, winy, BufferedImage.TYPE_INT_ARGB);
    }

    public void build(float[][] heightmap1, int seed1, String filename) throws IllegalArgumentException, IOException, BadHeightMapException {
        if (filename == null) {throw new IllegalArgumentException("no filename given");}
        heightmap = heightmap1;
        mapx = heightmap.length;
        mapy = heightmap[0].length;
        mapadjustx = (mapx - winx)/2;
        mapadjusty = (mapy - winy)/2;
        int sign = (int) Math.signum(heightmap[0][0]);
        for (int x=0; x<mapx; x++) {
            for (int y=0; y<mapy; y++) {
                if (heightmap[x][y] >= observerdist) {
                    throw new BadHeightMapException(BadHeightMapException.which.TOOHIGH, x, y);
                }
                if (sign*heightmap[x][y] <= 0) {
                    throw new BadHeightMapException(BadHeightMapException.which.NOTNONZERO, x, y);
                }
            }
        }
        int[] nointersect = verifyBorder(heightmap);
        if (nointersect != null) {throw new BadHeightMapException(BadHeightMapException.which.TOOSMALL, nointersect[0], nointersect[1]);}
        f.makeNew(seed1);
        for (int x1 = 0; x1 < winx; x1++) {
            for (int y1 = 0; y1 < winy; y1++) {
                agradient[x1][y1] = getpixelvalue(x1, y1);
            }
        }
        applyColorScheme(cs, filename);
    }

    public int[] verifyBorder(float[][] heightmap1) {
        heightmap = heightmap1;
        mapx = heightmap.length;
        mapy = heightmap[0].length;
        mapadjustx = (mapx - winx)/2;
        mapadjusty = (mapy - winy)/2;
        float[] intersectionleft;
        float[] intersectionright;
        for (int side1=0; side1 < winy; side1++) {
            intersectionleft = matchnext(mapadjustx, side1+mapadjusty, Direction.LEFT);
            intersectionright = matchnext(mapadjustx, side1+mapadjusty, Direction.RIGHT);
            if (intersectionleft == null && intersectionright == null) {
                return new int[] {0, side1};
            }
            intersectionleft = matchnext(winx+mapadjustx, side1+mapadjusty, Direction.LEFT);
            intersectionright = matchnext(winx+mapadjustx, side1+mapadjusty, Direction.RIGHT);
            if (intersectionleft == null && intersectionright == null) {
                return new int[] {winx, side1};
            }
        }
        for (int side1=0; side1 < winx; side1++) {
            intersectionleft = matchnext(side1+mapadjustx, mapadjusty, Direction.LEFT);
            intersectionright = matchnext(side1+mapadjustx, mapadjusty, Direction.RIGHT);
            if (intersectionleft == null && intersectionright == null) {
                return new int[] {side1, 0};
            }
            intersectionleft = matchnext(side1+mapadjustx, winy+mapadjusty, Direction.LEFT);
            intersectionright = matchnext(side1+mapadjustx, winy+mapadjusty, Direction.RIGHT);
            if (intersectionleft == null && intersectionright == null) {
                return new int[] {side1, winy};
            }
        }
        return null;
    }

    public void applyColorScheme(ColorScheme cs1, String filename) throws IOException, IllegalArgumentException{
        if (filename == null) {throw new IllegalArgumentException("no filename given");}
        File autos = new File(filename);
        for (int x1 = 0; x1 < winx; x1++) {
            for (int y1 = 0; y1 < winy; y1++) {
                b1.setRGB(x1, y1, cs1.giveColor(agradient[x1][y1]));
            }
        }
        ImageIO.write(b1, "png", autos);
    }

    private float calculate1(float x, float y, float z) {
        float d1 = f.calculate(x,y,z);
        if (x < border) {
            d1 = d1 * ((float) x/(float) border);
        }
        if (x > mapx - border) {
            d1 = d1 * ((float) (mapx - x)/(float) border);
        }
        return d1;
    }

    private float[] matchnext(float xstart, float ystart, Direction d) {
        //Takes in coordinates of the map (heightmap)
        float lowerx = xstart;
        float lowery = ystart;
        float lowerz = 0.0f;
        float upperx;
        float uppery = mapy/2.0f;
        float upperz = observerdist;
        if (d == Direction.LEFT) {
            upperx = (mapx - eyedist)/2.0f;
        } else {
            upperx = (mapx + eyedist)/2.0f;
        }
        float starting = -1.0f;
        if (lowerx == upperx && lowery == uppery) {
            //if starting position is right under the eye then the code below won't work, the intersection  on the surface is just the same point
            if (lowerx >= 0.0f && lowerx < mapx && lowery >= 0.0f && lowery < mapy) {
                upperz = calculate3(lowerx, lowery);
                lowerz = upperz;
                starting = upperz;
            } else {return null; } //should only run if completely stupid parameters are entered
        }
        //in order for bisection method to function we need to find a point below the surface, or a point outside the range of heightmap
        while (lowerz > starting && (lowerx >= 0.0f && lowerx < mapx && lowery >= 0.0f && lowery < mapy)) {
            starting = calculate3(lowerx, lowery);
            if (lowerz > starting) {
                lowerx = 2*lowerx-upperx;
                lowery = 2*lowery-uppery;
                lowerz = 2*lowerz-upperz;
            }
        }
        //now perform bisection method
        while (upperz-lowerz > 0.001f) {
            float midx = (lowerx + upperx)/2.0f;
            float midy = (lowery + uppery)/2.0f;
            if (midx < 0.0f || midx >= mapx || midy < 0.0f || midy >= mapy) {
                lowerx = midx;
                lowery = midy;
                lowerz = (lowerz+upperz)/2.0f;
                continue;
            }
            float midh = calculate3(midx, midy);
            if (midh > (lowerz+upperz)/2.0f) {
                lowerx = midx;
                lowery = midy;
                lowerz = (lowerz+upperz)/2.0f;
            } else {
                upperx = midx;
                uppery = midy;
                upperz = (lowerz+upperz)/2.0f;
            }
        }
        //If (lowerx, lowery) is outside the range of heightmap at this point, then it's very likely that the ray drawn from the eye through the inital point never intersects the surface
        if (lowerx <= 0.0f || lowerx-mapx >= 0.0f|| lowery <= 0.0f|| lowery-mapy >= 0.0f) {
            return null;
            //>= is needed because having too high precision in the previous step could hypothetically cause any of those terms to be 0.0f
        }
        lowerx = (lowerx+upperx)/2.0f;
        lowery = (lowery+uppery)/2.0f;
        lowerz = (lowerz + upperz)/2.0f;
        return new float[] {lowerx, lowery, lowerz};
    }

    private float calculate3(float x1, float y1) {
        int xi = (int) Math.floor(x1);
        int yi = (int) Math.floor(y1);
        if (xi == mapx-1) {
            if (yi == mapy-1) {
                return heightmap[xi][yi];
            } else {
                return (y1-yi)*heightmap[xi][yi+1]+(yi+1-y1)*heightmap[xi][yi];
            }
        } else if (yi == mapy-1) {
            return (x1-xi)*heightmap[xi+1][yi]+(xi+1-x1)*heightmap[xi][yi];
        } else {
            return ((x1-xi)*heightmap[xi+1][yi+1]+(xi+1-x1)*heightmap[xi][yi+1])*(y1-yi)
                    + ((x1-xi)*heightmap[xi+1][yi]+(xi+1-x1)*heightmap[xi][yi])*(yi+1-y1);
        }
    }

    private float getpixelvalue(int x3, int y3) {
        float pixelvalue = 0.0f;
        float nextpointx = x3+mapadjustx;
        float[] intersection = matchnext(nextpointx, y3+mapadjusty, Direction.LEFT);
        while (intersection != null) {
            float factor = observerdist/(observerdist - intersection[2]);
            pixelvalue += calculate1(intersection[0], intersection[1], intersection[2]);
            nextpointx = (mapx+eyedist)/2.0f + (intersection[0] - (mapx+eyedist)/2.0f)*factor;
            intersection = matchnext(nextpointx, y3+mapadjusty, Direction.LEFT);
        }
        nextpointx = x3+mapadjustx;
        intersection = matchnext(nextpointx, y3+mapadjusty, Direction.RIGHT);
        while (intersection != null) {
            float factor = observerdist/(observerdist - intersection[2]);
            pixelvalue += calculate1(intersection[0], intersection[1], intersection[2]);
            nextpointx = (mapx-eyedist)/2.0f + (intersection[0] - (mapx-eyedist)/2.0f)*factor;
            intersection = matchnext(nextpointx, y3+mapadjusty, Direction.RIGHT);
        }
        pixelvalue = (((pixelvalue % 2.0f) + 2.0f) % 2.0f);
        if (pixelvalue >= 1.0f) {
            pixelvalue = 2.0f-pixelvalue;
        }
        return pixelvalue;
    }
}
