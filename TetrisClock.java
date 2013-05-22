import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Duncan
 * Date: 10/02/13
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
class TetrisClock extends Animation implements Observer{
    PApplet parent;
    long lastTime = System.nanoTime();
    long timer = System.currentTimeMillis();
    final double ns = 1000000000.0 / 5.0f; //FPS
    double delta = 0;
    int updates = 0;
    int frames = 0;

    boolean blink = true;

    //clock stuff
    number h1, h2, m1, m2, div;
    int m = parent.minute();
    int h = parent.hour();
    int s = parent.second();


    Grid grid = new Grid();

    TetrisClock(Flipdot p){
        super(p);
        parent = p;
        h1 = new number(3);
        h2 = new number(3+9);
        div = new number(22);
        m1 = new number(35-8);
        m2 = new number(36);
        showTime();
        p.addObserver(this);
    }

    PGraphics drawAnimation(PGraphics pg){
        grid.drawAnimation(pg);
        h1.draw(pg);
        h2.draw(pg);
        div.draw(pg);
        m1.draw(pg);
        m2.draw(pg);
        frames++;
        if(System.currentTimeMillis() - timer > 1000){
            frames = 0;
            updates = 0;
            timer += 1000;
            blink =! blink;
    }
        hideColon(pg);
        return pg;
    }

    void updateAnimation(){

        int deltaM = parent.minute();
        int deltaH = parent.hour();
        long now = System.nanoTime();
        delta += (now - lastTime) / ns;
        lastTime = now;
        while (delta >= 1){

            for(Pieces Piece : h1.piece) Piece.stepDown();
            for(Pieces Piece : h2.piece) Piece.stepDown();
            for(Pieces Piece : div.piece) Piece.stepDown();
            for(Pieces Piece : m1.piece) Piece.stepDown();
            for(Pieces Piece : m2.piece) Piece.stepDown();

            if(deltaM != m){
                int ddM=divide(deltaM,10);
                int dM=divide(m,10);
                m=deltaM;
                if(ddM!=dM){
                    m1.destroy();
                    parent.println("Destroying m1");
                    if(dM<6)m1.setNumber(ddM);
                    else m1.setNumber(0);
                }
                m2.destroy();
                m2.setNumber(m - ddM * 10);
                if(deltaH != h){
                    m1.destroy();
                    m2.destroy();
                    h1.destroy();
                    h2.destroy();
                    showTime();
                }
            }
            delta--;
            updates++;
        }
    }

    void resetAnimation(){
        m1.destroy();
        m2.destroy();
        div.destroy();
        h1.destroy();
        h2.destroy();
        timer = System.currentTimeMillis() + 2000;
        showTime();
    }

    public void showTime(){
        h = parent.hour();
        m = parent.minute();
        s = parent.second();

        if(h<10){
            h1.setNumber(0);
            h2.setNumber(h);
        }else if(h >= 10 && h < 20){
            h1.setNumber(1);
            h2.setNumber(h - 10);
        }else{
            h1.setNumber(2);
            h2.setNumber(h - 20);
        }
        //Set minutes
        int dM=divide(m,10);
        if(dM<6)m1.setNumber(dM);
        else m1.setNumber(0);
        m2.setNumber(m - dM * 10);
        div.setNumber(10);
    }

    public int divide(int a, int b){
        int z = 0;
        int i = a;

        while (i>= b){
            i = i - b;
            z++;
        }return z;
    }

    public void update(char keyCode) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public PGraphics hideColon(PGraphics pg){
        if(blink){
            int pixels = 4;
            int width = xbound;
            int height = ybound;
            int rows = height/pixels;
            int cols = width/pixels;
            int offset = 23;
            pg.pushStyle();
            pg.rectMode(CORNER);
            pg.fill(0);
            pg.rect(offset * pixels,0,pixels*4,height);
            pg.popStyle();
        }
        return pg;
    }

    class number{
        Shape[] shapes = new Shape[7];
        List<Pieces> piece = new ArrayList<Pieces>();

        int offset;
        int k;

        number(int offset){
            this.offset = offset;
            shapes[0] = new Shape(4, new int[] {8,9,10,11});  // I
            shapes[1] = new Shape(3, new int[] {0,3,4,5});  // J
            shapes[2] = new Shape(3, new int[] {2,3,4,5});  // L
            shapes[3] = new Shape(2, new int[] {0,1,2,3});  // O
            shapes[4] = new Shape(4, new int[] {5,6,8,9});  // S
            shapes[5] = new Shape(3, new int[] {1,3,4,5,});  // T
            shapes[6] = new Shape(4, new int[] {4,5,9,10});  // Z
        }

        void setNumber(int num){
            if (num==0){
                zero();
            }  else if(num==1){
                one();
            }   else if(num==2){
                two();
            }  else if(num==3){
                three();
            }  else if(num==4){
                four();
            }  else if(num==5){
                five();
            }   else if(num==6){
                six();
            }  else if(num==7){
                seven();
            }  else if(num==8){
                eight();
            }  else if(num==9){
                nine();
            }  else if(num==10){
                div();
            }
        }

        PGraphics draw(PGraphics pg){
            for(Pieces Piece : piece) {
                pg = Piece.draw(pg);
                Piece.update();
            }
            return pg;
        }

        void loadPiece(int i){
            piece.add(new Pieces(shapes[i]));
        }

        void destroy(){
            piece.removeAll(piece);
            grid.clearOffset(offset);
        }

        void rotatePiece(int count){
            if(!piece.isEmpty()){
                Pieces Piece = piece.get(piece.size()-1);
                for (int i = 0; i < count; i++) Piece.rotate();
            }
        }

        void movePiece(int x, int y){
            if(!piece.isEmpty()){
                Pieces Piece = piece.get(piece.size()-1);
                Piece.x = x; Piece.y = y;
            }
        }
        void setDestX(int destX){
            if(!piece.isEmpty()){
                Pieces Piece = piece.get(piece.size()-1);
                Piece.destX = destX;
            }
        }

        void setArtificalFloor(int af){
            if(!piece.isEmpty()){
                Pieces Piece = piece.get(piece.size()-1);
                Piece.setArtificalFloor(af);
            }
        }

        void div(){
            loadPiece(3);
            setArtificalFloor(2);
            movePiece(1 + offset, -2);
            setDestX(1+offset);
            loadPiece(3);
            setArtificalFloor(2);
            movePiece(1+offset, -10);
            setDestX(1+offset);
        }

        void one(){
            loadPiece(2); //New L piece
            movePiece(2 + offset, 4);
            setDestX(4+offset);
            rotatePiece(1);
            loadPiece(2);
            movePiece(6+ offset, -2);
            setDestX(5+offset);
            rotatePiece(3);
            loadPiece(3);
            movePiece(3+ offset, -10);
            setDestX(5+offset);
            loadPiece(2);
            movePiece(2+ offset,-15);
            setDestX(4+offset);
            rotatePiece(1);
            loadPiece(2);
            movePiece(6+ offset,-20);
            setDestX(5+offset);
            rotatePiece(3);
        }

        void two(){
            loadPiece(0);//I
            movePiece(3+ offset,0);
            setDestX(3+ offset);
            loadPiece(1);//J
            rotatePiece(2);
            movePiece(5+ offset,-5);
            setDestX(5+ offset);
            loadPiece(0);//I
            rotatePiece(1);
            movePiece(1+ offset,-10);
            setDestX(1+ offset);
            loadPiece(2);//L
            movePiece(2+ offset,-15);
            setDestX(2+ offset);
            rotatePiece(1);
            loadPiece(0);
            movePiece(3+ offset,-19);
            setDestX(3+ offset);
            loadPiece(2);
            movePiece(2+ offset,-23);
            setDestX(2+ offset);
            rotatePiece(2);
            loadPiece(1);
            movePiece(5+ offset,-27);
            setDestX(5+ offset);
            rotatePiece(2);
            loadPiece(3);//O
            movePiece(6+ offset,-31);
            setDestX(6+ offset);
            loadPiece(0);
            movePiece(3+ offset,-35);
            setDestX(3+ offset);
            loadPiece(1);
            movePiece(5+ offset,-39);
            setDestX(5+ offset);
            rotatePiece(2);
            loadPiece(2);
            movePiece(2 + offset,-42);
            setDestX(2+ offset);
            rotatePiece(2);
        }

        void three(){
            loadPiece(1);
            movePiece(3 + offset,0);
            setDestX(3+ offset);
            loadPiece(3);
            movePiece(1 + offset,-5);
            setDestX(1+ offset);
            loadPiece(1);
            rotatePiece(3);
            movePiece(4 + offset,-10);
            setDestX(4+ offset);
            loadPiece(0);
            movePiece(2 + offset,-15);
            setDestX(2+ offset);
            loadPiece(0);
            rotatePiece(1);
            movePiece(5 + offset,-19);
            setDestX(5+ offset);
            loadPiece(2);
            movePiece(1 + offset,-23);
            setDestX(1+ offset);
            rotatePiece(2);
            loadPiece(1);
            movePiece(4 + offset,-27);
            setDestX(4+ offset);
            rotatePiece(2);
            loadPiece(3);
            movePiece(5 + offset,-32);
            setDestX(5+ offset);
            loadPiece(0);
            movePiece(2 + offset,-36);
            setDestX(2+ offset);
            loadPiece(2);
            movePiece(1 + offset,-39);
            setDestX(1+ offset);
            rotatePiece(2);
            loadPiece(1);
            movePiece(4 + offset,-42);
            setDestX(4+ offset);
            rotatePiece(2);
        }

        void four(){
            loadPiece(1);
            movePiece(5 + offset,0);
            setDestX(5 + offset);
            rotatePiece(3);
            loadPiece(1);
            movePiece(4 + offset,-5);
            setDestX(4+ offset);
            rotatePiece(1);
            loadPiece(0);
            movePiece(2 + offset,-10);
            setDestX(2+ offset);
            loadPiece(2);
            movePiece(1 + offset,-15);
            setDestX(1+ offset);
            rotatePiece(2);
            loadPiece(1);
            movePiece(4 + offset,-19);
            setDestX(4+ offset);
            rotatePiece(2);
            loadPiece(0);
            movePiece(5 + offset,-23);
            setDestX(5+ offset);
            rotatePiece(1);
            loadPiece(2);
            movePiece(0 + offset,-27);
            setDestX(0+ offset);
            rotatePiece(1);
            loadPiece(0);
            movePiece(4 + offset,-31);
            setDestX(4+ offset);
            rotatePiece(1);
            loadPiece(2);
            movePiece(1 + offset,-35);
            setDestX(1+ offset);
            rotatePiece(3);
        }

        void five(){
            loadPiece(1);
            movePiece(3 + offset,0);
            setDestX(3+ offset);
            loadPiece(3);
            movePiece(1 + offset,-5);
            setDestX(1+ offset);
            loadPiece(1);
            rotatePiece(3);
            movePiece(4 + offset,-10);
            setDestX(4+ offset);
            loadPiece(0);
            movePiece(2 + offset,-15);
            setDestX(2+ offset);
            loadPiece(0);
            rotatePiece(1);
            movePiece(5 + offset,-19);
            setDestX(5+ offset);
            loadPiece(2);
            movePiece(1 + offset,-23);
            setDestX(1+ offset);
            rotatePiece(2);
            loadPiece(3);
            movePiece(1 + offset,-27);
            setDestX(1+ offset);
            loadPiece(1);
            movePiece(4 + offset,-32);
            setDestX(4+ offset);
            rotatePiece(2);
            loadPiece(0);
            movePiece(2 + offset,-36);
            setDestX(2+ offset);
            loadPiece(2);
            movePiece(1 + offset,-39);
            setDestX(1+ offset);
            rotatePiece(2);
            loadPiece(1);
            movePiece(4 + offset,-42);
            setDestX(4+ offset);
            rotatePiece(2);
        }

        void six(){
            loadPiece(1);
            movePiece(1 + offset,0);
            setDestX(1+ offset);
            loadPiece(5);
            movePiece(4 + offset,-5);
            setDestX(4+ offset);
            loadPiece(4);
            movePiece(4 + offset,-10);
            setDestX(4+ offset);
            rotatePiece(1);
            loadPiece(1);
            movePiece(2 + offset,-15);
            setDestX(2+ offset);
            loadPiece(5);
            movePiece(5 + offset,-20);
            setDestX(5+ offset);
            rotatePiece(3);
            loadPiece(4);
            movePiece(3 + offset,-25);
            setDestX(3+ offset);
            loadPiece(1);
            movePiece(1 + offset,-30);
            setDestX(1+ offset);
            rotatePiece(1);
            loadPiece(0);
            movePiece(0 + offset,-35);
            setDestX(0+ offset);
            rotatePiece(1);
            loadPiece(1);
            movePiece(1 + offset,-40);
            setDestX(1+ offset);
            rotatePiece(3);
            loadPiece(1);
            movePiece(0 + offset,-45);
            setDestX(0+ offset);
            rotatePiece(1);
        }

        void seven(){
            loadPiece(3);
            movePiece(5 + offset,0);
            setDestX(5+ offset);
            loadPiece(2);
            movePiece(4 + offset,-5);
            setDestX(4+ offset);
            rotatePiece(1);
            loadPiece(2);
            movePiece(5 + offset,-10);
            setDestX(5+ offset);
            rotatePiece(3);
            loadPiece(3);
            movePiece(5 + offset,-15);
            setDestX(5+ offset);
            loadPiece(0);
            movePiece(2 + offset,-19);
            setDestX(2+ offset);
            loadPiece(2);
            movePiece(1 + offset,-24);
            setDestX(1+ offset);
            rotatePiece(2);
            loadPiece(1);
            movePiece(4 + offset,-27);
            setDestX(4+ offset);
            rotatePiece(2);
        }

        void eight(){
            loadPiece(1);
            movePiece(1 + offset,0);
            setDestX(1+ offset);
            loadPiece(2);
            movePiece(4 + offset,-5);
            setDestX(4+ offset);
            loadPiece(6);
            movePiece(1 + offset,-10);
            setDestX(1+ offset);
            loadPiece(4);
            movePiece(4 + offset,-14);
            setDestX(4+ offset);
            loadPiece(1);
            rotatePiece(3);
            movePiece(5 + offset,-18);
            setDestX(5+ offset);
            loadPiece(2);
            rotatePiece(1);
            movePiece(0 + offset,-23);
            setDestX(0+ offset);
            loadPiece(0);
            movePiece(2 + offset,-26);
            setDestX(2+ offset);
            loadPiece(2);
            movePiece(3 + offset,-31);
            setDestX(3+ offset);
            loadPiece(5);
            rotatePiece(3);
            movePiece(1 + offset,-36);
            setDestX(1+ offset);
            loadPiece(5);
            movePiece(2 + offset,-40);
            setDestX(2+ offset);
            loadPiece(1);
            movePiece(0 + offset,-43);
            setDestX(0+ offset);
            rotatePiece(1);
            loadPiece(0);
            movePiece(5 + offset,-46);
            setDestX(5+ offset);
            rotatePiece(1);
            loadPiece(2);
            movePiece(4 + offset,-49);
            setDestX(4+ offset);
            rotatePiece(3);
        }

        void nine(){
            loadPiece(1);
            movePiece(5 + offset,0);
            setDestX(5+ offset);
            rotatePiece(3);
            loadPiece(1);
            movePiece(4 + offset,-5);
            setDestX(4+ offset);
            rotatePiece(1);
            loadPiece(0);
            movePiece(2 + offset,-10);
            setDestX(2+ offset);
            loadPiece(5);
            movePiece(0 + offset,-14);
            setDestX(0+ offset);
            rotatePiece(1);
            loadPiece(2);
            movePiece(3 + offset,-19);
            setDestX(3+ offset);
            loadPiece(0);
            movePiece(5 + offset,-24);
            setDestX(5+ offset);
            rotatePiece(1);
            loadPiece(4);
            movePiece(0 + offset,-28);
            setDestX(0+ offset);
            rotatePiece(1);
            loadPiece(1);
            movePiece(3 + offset,-32);
            setDestX(3+ offset);
            rotatePiece(2);
            loadPiece(1);
            movePiece(4 + offset,-36);
            setDestX(4+ offset);
            rotatePiece(2);
            loadPiece(5);
            movePiece(1 + offset,-40);
            setDestX(1+ offset);
            rotatePiece(2);
        }

        void zero(){
            //Note setDestX sets the real value, movepiece x sets the start point
            loadPiece(0);
            movePiece(offset,0);
            setDestX(2+offset);
            loadPiece(0);
            rotatePiece(1);
            movePiece(offset+2,-5);
            setDestX(6+offset);
            loadPiece(5);
            movePiece(offset-2,-10);
            setDestX(2+offset);
            loadPiece(6);
            movePiece(offset+1,-15);
            setDestX(1+offset);
            rotatePiece(1);
            loadPiece(5);
            movePiece(offset+2,-20);
            setDestX(5+offset);
            rotatePiece(3);
            loadPiece(6);
            movePiece(offset,-25);
            setDestX(1+offset);
            rotatePiece(1);
            loadPiece(6);
            rotatePiece(1);
            movePiece(offset+3,-30);
            setDestX(5+offset);
            loadPiece(5);
            rotatePiece(1);
            movePiece(offset-1,-35);
            setDestX(1+offset);
            loadPiece(6);
            movePiece(offset+2,-40);
            setDestX(2+offset);
            loadPiece(5);
            rotatePiece(1);
            movePiece(offset+4,-45);
            setDestX(5+offset);
            loadPiece(1);
            movePiece(offset+4,-50);
            setDestX(5+offset);
            rotatePiece(2);
            loadPiece(0);
            movePiece(offset+4,-55);
            setDestX(4+offset);
        }
    }

    class Shape{
        boolean[][] matrix;
        int c;

        Shape (int n,int [] blockNums) {
            matrix = new boolean[n][n];
            for (int x = 0; x<n; x++){
                for (int y=0; y<n; y++){
                    matrix[x][y] = false;
                }
            }
            for (int i=0; i<blockNums.length;i++){
                matrix[blockNums[i]%n][blockNums[i]/n] = true;
            }
            for (int x = 0; x<n; x++){
                for (int y=0; y<n; y++){
//                    parent.print(matrix[x][y]? 1:0) ;
                }//parent.println();
            }
        }

        Shape(Shape other){
            matrix = new boolean[other.matrix.length][other.matrix.length];
            for (int x=0; x<matrix.length;x++){
                for(int y=0;y<matrix.length;y++) {
                    matrix[x][y] = other.matrix[x][y];
                }
            }
        }

    }

    class Grid {
        int width, height;
        int rows, cols;
        int pixels = 4;
        boolean[][] pieces;
        int artificalFloor = 2;

        int animateCount = -1;

        Grid(){
            this.width = xbound;
            this.height = ybound;
            this.rows = height/pixels;
            this.cols = width/pixels;
            pieces = new boolean[cols][rows];
            for (int i=0;i<cols;i++){
                for (int j=0;j<rows;j++){
                    pieces[i][j] = false;
                }
            }

        }

        void clear(){
            for (int i=0;i<cols;i++){
                for (int j=0;j<rows;j++){
                    pieces[i][j] = false;
                }
            }
        }

        void clearOffset(int offset){
            for(int i = offset; i<offset+8;i++){
                for (int j=0;j<rows;j++){
                    pieces[i][j]=false;
                }
            }
        }

        PGraphics drawAnimation(PGraphics pg){
            pg.fill(128);
            pg.noStroke();
            pg.rect(0,0,width,height);
            //pg.fill(255);
            for (int i=0;i<cols;i++){
                for (int j=0;j<rows;j++){
                    fillSquare(i,j, pieces[i][j], pg);
                }
            }
            return pg;
        }

        PGraphics fillSquare(int col, int row, boolean c, PGraphics pg){
            if (col<0||col>=cols||row<0||row>=rows){return pg;}
            int px = c ? 255 : 0;
            pg.fill(px);
            pg.strokeWeight(1);
            pg.stroke(0);

            pg.rect(col*(width/cols),row*(height/rows),width/cols,height/rows);

            return pg;
        }

        void loadPiece(int x, int y){
            pieces[x][y]=true;
        }

        void eraseCleared(){

        }

        boolean isOccupied(int x, int y){
            boolean b;
            if (y<0 && x< cols && x >= 0){
                return  false;
            }
            return (x >= cols || x < 0 || y>= rows - artificalFloor || pieces[x][y] != false);
        }
    }

    class Pieces{
        //Here's where we declare each of the shapes
        Shape shape;
        int x, y;
        int destX, destY;
        int finalRow;
        int artificalFloor = 0;

        Pieces(Shape shape){
            this.shape = new Shape(shape);
            x = 3;
            y = -2;
            finalRow = getFinalRow();
        }

        void stepDown(){
            if (y >= finalRow) {
                endTurn();
            } else if (y >= finalRow - artificalFloor){
                endTurn();
            }else {
                y ++;
                //TetrisClock.this.currTime = 0;
            }
            if(y>0){
                if(x<destX){
                    x++;
                }
                if(x>destX){
                    x--;
                }
            }
        }

        void update(){
            finalRow = getFinalRow();
        }

        void setArtificalFloor(int af){
            artificalFloor = af;
        }

        void left(){x--;}
        void right(){x++;}
        void down(){y++;}
        void rotate(){
            boolean[][] ret = new boolean[shape.matrix.length][shape.matrix.length];
            for (int x=0;x<ret.length;x++){
                for(int y=0;y<ret.length;y++){
                    ret[x][y]=shape.matrix[y][ret.length-1-x];
                }
            }
            if (isLegal(ret, x, y)) {
                shape.matrix = ret;
                update();
            } else if (isLegal(ret, x + 1, y) || isLegal(ret, x + 2, y)) {
                shape.matrix = ret;
                right();
            } else if (isLegal(ret, x - 1, y) || isLegal(ret, x - 2, y)) {
                shape.matrix = ret;
                left();
            }
        }

        void move(){
            if (x > destX){

            }
        }

        int getFinalRow() {
            int start = parent.max(0, y);
            int row = start;
            while (row <= grid.rows) {
                if (!isLegal(shape.matrix, x, row)){
                    return row - 1;
                }
                row++;
            }
            return - 1;
        }

        void endTurn(){
            for (int i=0; i < shape.matrix.length; i++){
                for (int j=0; j < shape.matrix.length; j++){
                    if (shape.matrix[i][j] && j + y >= 0){
                        grid.pieces[i + x][j+y] = true;
                    }
                }
            }
            update();
        }

        boolean isLegal(boolean [][] matrix, int col, int row){
            for (int i=0;  i< matrix.length; i++){
                for (int j=0; j < matrix.length; j++){
                    if (matrix[i][j] && grid.isOccupied(col + i, row + j)){
                        return false;
                    }
                }
            }
            return true;
        }


        PGraphics draw(PGraphics pg){
            for (int i=0; i<shape.matrix.length;i++){
                for (int j=0;j<shape.matrix.length;j++){
                    if (shape.matrix[i][j]){
                        pg = grid.fillSquare(x+i,y+j,shape.matrix[i][j],pg);
                    }
                }
            }
            return pg;
        }

    }
}
