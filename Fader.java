/**
 * Created with IntelliJ IDEA.
 * User: Duncan
 * Date: 28/01/13
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */
import processing.core.*;
import java.util.*;

public class Fader{
    public PImage src1; //Start image
    public PImage src2; //End image
    public PGraphics alphaMask;
    PApplet parent;
    public int x, y;
    boolean t = true;
    double tmp;

    Fader(PApplet p){
        parent = p;
        alphaMask = new PGraphics();
        src1 = new PImage();
        src2 = new PImage();
    }

    public void setImages(PImage sr1, PImage sr2) {
        src1=sr1;
        src2=sr2;
        alphaMask = parent.createGraphics(src1.width, src1.height);
        startFade();
    }

    public void setXY(int x_, int y_){
        x = x_;
        y = y_;
    }

    /**
     * called to initialise fade. This default implementation starts
     * with a black mask. Subclasses can override this to do other initialisation.
     */

    public void startFade() {
        alphaMask.beginDraw();
        alphaMask.fill(0);
        alphaMask.stroke(0);
        alphaMask.rect(0, 0, src1.width, src1.height);
        alphaMask.endDraw();
    }

    /**
     * called to end the fade. This default implementation creates a 100% white mask.
     * May not be needed, but I've had some problems with rounding errors
     * and arc drawing leaving gaps. Calling this will
     * guarantee you see 100% final image.
     */

    public void endFade() {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        alphaMask.rect(0, 0, src1.width, src1.height);
        alphaMask.endDraw();
    }

    /**
     * called for each frame of the animation. pass in the percentage (0.0-1.0) of the effect.
     * this should be overridden for each effect subclass.
     */

    public void animateFade(float percent) {
        ;
    }

    /**
     * Renders blended images. You shouldn't need to override this.
     */

    public PGraphics draw(PGraphics pg) {
        pg.loadPixels();
        pg.beginDraw();
        src1.loadPixels();
        src2.loadPixels();
        int w=src1.width;
        for (int ix=0;ix<src1.pixels.length;ix++) {
            int y=ix/w;
            int x=ix%w;
            int col1=src1.pixels[ix];
            int r1=(col1&0xFF0000)>>16;
            int g1=(col1&0xFF00)>>8;
            int b1=(col1&0xFF);
            int col2=src2.pixels[ix];
            int r2=(col2&0xFF0000)>>16;
            int g2=(col2&0xFF00)>>8;
            int b2=(col2&0xFF);
            int colmask= alphaMask.pixels[ix];
            int level=(colmask&0xFF); // use blue channel
            float lev= (float)level/256.0f;
            int rr=(int)((r1*lev)+(r2*(1-lev)));
            int gg=(int)((g1*lev)+(g2*(1-lev)));
            int bb=(int)((b1*lev)+(b2*(1-lev)));
            pg.pixels[ix]=0xFF000000|rr<<16|gg<<8|bb;
        }
        pg.updatePixels();
        pg.endDraw();
        return pg;
    }
}

class CrossFade extends Fader {
    CrossFade(PApplet p) {
        super(p);
    }

    // simple crossfade. Mask is gradually changed from black (first image)
    // to white (second image)

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255 * percent);
        alphaMask.stroke(255 * percent);
        alphaMask.rect(0, 0, src1.width, src1.height);
        alphaMask.endDraw();
    }
}

class DownFade extends Fader {
    DownFade(PApplet p) {
        super(p);
    }
    // Second image is introduced from the top

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        alphaMask.rect(0, 0, src1.width, src1.height * percent);
        alphaMask.endDraw();
    }
}

class EllipseFade extends Fader {
    EllipseFade(PApplet p) {
        super(p);
    }

    // second image is introduced from the centre as an ellipse

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        parent.strokeWeight(3);
        parent.ellipseMode(PConstants.CENTER);
        alphaMask.ellipse(src1.width / 2.0f, src1.height / 2f, src1.width * 5f * percent, src1.height * 5f * percent);
        alphaMask.endDraw();
    }
}

class EllipseFadePoint extends Fader {
    EllipseFadePoint(PApplet p){
        super(p);
    }

    public void animateFade(float percent){
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        alphaMask.strokeWeight(3);
        alphaMask.ellipseMode(PConstants.CENTER);
        alphaMask.ellipse(x, y, src1.height * 5f * percent, src1.height * 5f * percent);
        alphaMask.endDraw();
    }
}

class EllipseFadePointReverse extends Fader {
    EllipseFadePointReverse(PApplet p){
        super(p);
    }

    public void animateFade(float percent){
        alphaMask.beginDraw();
        alphaMask.background(255);
        alphaMask.fill(0);
        alphaMask.stroke(0);
        alphaMask.strokeWeight(3);
        alphaMask.ellipseMode(PConstants.CENTER);
        alphaMask.ellipse(x, y, src1.height * 5f * (1-percent), src1.height * 5f * (1-percent));
        alphaMask.endDraw();
    }
}

class LeftFade extends Fader {
    LeftFade(PApplet p) {
        super(p);
    }
    // second image is introduced from the left to right

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        alphaMask.rect(0, 0, src1.width * percent, src1.height);
        alphaMask.endDraw();
    }
}

class RandomBlockFade extends Fader {
    // second image is introduced using random 8x8 pixel blocks

    boolean[][] used;

    RandomBlockFade(PApplet p) {
        super(p);
    }

    public void startFade() {
        used=new boolean[200][200];
        super.startFade();
    }

    public void animateFade(float percent) {
        int numcells=(src1.height/8)*(src1.width/8);
        if (percent==0.0f) used=new boolean[200][200];
        for (int i=0;i<(int)((float)numcells*percent);i++) {
            int cellx=(int)parent.random((src1.width / 8.0f) + 1);
            int celly=(int)parent.random((src1.height / 8.0f) + 1);
            used[celly][cellx]=true;
        }

        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        for (int y=0;y<=src1.height/8;y++) {
            for (int x=0;x<=src1.width/8;x++) {
                if (used[y][x]) {
                    alphaMask.fill(255);
                    alphaMask.stroke(255);
                } else {
                    alphaMask.fill(0);
                    alphaMask.stroke(0);
                }
                alphaMask.rect(x * 8, y * 8, 8, 8);
            }
        }
        alphaMask.endDraw();
    }
}

class RightFade extends Fader {
    RightFade(PApplet p) {
        super(p);
    }
    // second image is introduced from the right

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        if(percent<=1.2){
            alphaMask.rect(src1.width - (src1.width * percent), 0, src1.width, src1.height);
        }else alphaMask.rect(0, 0, src1.width, src1.height);
        alphaMask.endDraw();
    }
}

class RotateBoxFader extends Fader {
    RotateBoxFader(PApplet p) {
        super(p);
    }
// second image is introduced using a growing spiral from the centre

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        alphaMask.pushMatrix();
        alphaMask.rectMode(parent.CENTER);
        alphaMask.translate(src1.width / 2, src1.height / 2);
        alphaMask.rotate((parent.TWO_PI * percent)/parent.frameRate);
        alphaMask.rect(0, 0, src1.width * percent, src1.height * percent);
        alphaMask.popMatrix();
        alphaMask.endDraw();
    }
}

class RotateFade extends Fader {
    RotateFade(PApplet p) {
        super(p);
    }
    // second image is introduced using a clockwise circular sweep from 3 o'clock

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        parent.strokeWeight(3);
        alphaMask.arc(src1.width / 2.0f, src1.height / 2f, src1.width * 2.0f, src1.height * 2.0f, 0.0f, parent.TWO_PI * percent);
        alphaMask.endDraw();
    }
}

class SanfuFade extends  Fader{
    SanfuFade (PApplet p){
        super(p);
    }

    public void animateFade(float percent){
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        parent.strokeWeight(3);
        float x;
        for (int y = 0; y < src1.height; y=y+3){
            if(percent < 0.5){
                x = src1.width * percent*2;
                if(y%3 == 0){
                    alphaMask.line(0,y,x,y);
                }
            } else if(percent > 0.4){
                if(y%3 == 0){
                    alphaMask.line(0,y,src1.width,y);
                }
                x = src1.width * (percent-0.5f)*2;
                alphaMask.rect(0,0,x,src1.height);
            }
        }
        alphaMask.endDraw();
    }
}

class SnakeFade extends Fader {
    SnakeFade(PApplet p) {
        super(p);
    }
    // second image is introduced from the left to right

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        alphaMask.strokeWeight(3);
        alphaMask.noSmooth();
        int lines = 4;
        float step=src1.pixels.length/(100*lines);
        //parent.println(step);
        float currPos = step*percent*100;
        double currRow = Math.floor((currPos/(float)src1.width)*lines);
        float PosX = (currPos*lines)-((float)currRow*src1.width);
        alphaMask.rect(0,0,src1.width,(float)currRow-lines/2);
        if(t){
            alphaMask.line(0, (float)currRow,PosX,(float)currRow);
            if(currRow!=tmp){
                t =! t;
                tmp = currRow;
            }
            //alphaMask.line(src1.width,(float)currRow,PosX,(float)currRow);
        } else {
            //alphaMask.line(0, (float)currRow,PosX,(float)currRow-1);
            alphaMask.line(src1.width-PosX,(float)currRow,src1.width,(float)currRow);
            if(currRow!=tmp){
                t =! t;
                tmp = currRow;
            }
        }
        alphaMask.endDraw();
    }
}

class SnakeFadeRight extends Fader {
    SnakeFadeRight(PApplet p) {
        super(p);
    }
    // second image is introduced from the left to right

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        alphaMask.strokeWeight(4);
        float step=src1.pixels.length/900;
        //parent.println(step);
        float currPos = step*percent*100;
        double currRow = Math.floor(currPos/(float)src1.width);
        float PosX = currPos-((float)currRow*src1.width);
        alphaMask.rect(0,0,src1.width,(float)currRow-1);
        alphaMask.line(0,(float)currRow,PosX,(float)currRow);
        alphaMask.endDraw();
    }
}

class waveFade extends Fader {
    waveFade(PApplet p) {
        super(p);
    }
    // second image is introduced from the left to right

    public void animateFade(float percent) {
        alphaMask.beginDraw();
        alphaMask.fill(255);
        alphaMask.stroke(255);
        alphaMask.strokeWeight(1);
        float x = 0;
        float wave = percent * 200;
        while (x < src1.width){
            float k = wave * parent.noise(x/100,percent);
            alphaMask.noStroke();
            alphaMask.ellipse(x,src1.height-k, 3, 3);
            alphaMask.stroke(255);
            alphaMask.strokeWeight(1);
            alphaMask.line(x,src1.height-k,x,src1.height);
            x++;
        }
        //parent.println(step);
        alphaMask.endDraw();
    }
}
