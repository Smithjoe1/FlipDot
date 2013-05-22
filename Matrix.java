import processing.core.PGraphics;
//
///**
// * Created with IntelliJ IDEA.
// * User: Duncan
// * Date: 14/03/13
// * Time: 4:29 PM
// * To change this template use File | Settings | File Templates.
//*/
//
class Matrix extends Animation{
    CellArray cells;

    Flipdot parent;

    Matrix(Flipdot p){
        cells = new CellArray(3);
        parent = p;
    }

    //This renders the animation
    public PGraphics drawAnimation(PGraphics pg) {
        cells.handle(pg);
        return pg;
    }

    //This updates the animation
    void updateAnimation(){
        cells.staticActivity();
        cells.action();
    }

    //This resets the animation
    void resetAnimation(){

    }


    class CellArray{
        float width;
        float height;
        float pixels;
        int count;
        Cell[][] cells;
        int[] offset;

        public CellArray(int pixels){
            this.width = xbound;
            this.height = ybound;
            this.pixels = pixels;
            int cols = (int)width/pixels;
            int rows = (int)height/pixels;

            cells = new Cell[cols][rows];
            for(int i = 0; i < cells.length; i++){
                for(int k = 0; k < cells[0].length; k++){
                    cells[i][k] = new Cell(pixels*i, pixels*k,pixels,pixels);
                }
            }
            offset = new int[cols];
            for(int i=0; i < offset.length; i++){
                offset[i] = (int)random(rows);
            }
        }

        public void action(){
            for(int i = 0; i < cells.length; i++){
                cells[i][((parent.frameCount/2)+offset[i])%cells[0].length].spark();
            }
        }

        public void staticActivity(){
            for (int i = 0; i < cells.length; i++){
                for(int k = 0; k < cells[0].length;k++){
                    cells[i][k].spark(random(5));
                }
            }
        }

        public PGraphics handle(PGraphics pg){
            for(int i = 0; i < cells.length; i++){
                for(int k = 0; k < cells[0].length; k++){
                    cells[i][k].handle(1.08f, pg);
                }
            }
            return pg;
        }
    }

    class Cell{
        float locX, locY;
        float sizeX, sizeY;
        float cBrightness = 0;

        Cell(float locX, float locY, float sizeX, float sizeY){
            this.locX = locX;
            this.locY = locY;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }

        public void spark(){
            cBrightness = 255;
        }

        public void spark(float bright){
            cBrightness += bright;
        }

        public PGraphics handle(float degredation, PGraphics pg){
            cBrightness /= degredation;
            pg.beginDraw();
            pg.pushStyle();
            pg.fill(cBrightness,cBrightness,cBrightness);
            pg.rect(locX,locY,sizeX,sizeY);
            pg.popStyle();
            pg.endDraw();
            return pg;
        }
    }
}
