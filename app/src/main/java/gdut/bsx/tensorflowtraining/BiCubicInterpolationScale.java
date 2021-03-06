package gdut.bsx.tensorflowtraining;

public class BiCubicInterpolationScale {

    private static double a00, a01, a02, a03;
    private static double a10, a11, a12, a13;
    private static double a20, a21, a22, a23;
    private static double a30, a31, a32, a33;
    private static int srcWidth;
    private static int srcHeight;

    /**
     * 双立方插值
     * @param inPixelsData 像素矩阵数组
     * @param srcW 原图像的宽
     * @param srcH 原图像的高
     * @param destW 目标图像的宽
     * @param destH 目标图像的高
     * @return 处理后的推三矩阵数组
     */
    public static int[] imgScale(int[] inPixelsData, int srcW, int srcH, int destW, int destH) {
        double[][][] input3DData = processOneToThreeDeminsion(inPixelsData, srcH, srcW);
        int[][][] outputThreeDeminsionData = new int[destH][destW][4];
        double[][] tempPixels = new double[4][4];
        float rowRatio = ((float)srcH)/((float)destH); //1/3
        float colRatio = ((float)srcW)/((float)destW);
        srcWidth = srcW;
        srcHeight = srcH;
        for(int row=0; row<destH; row++) {
            // convert to three dimension data
            double srcRow = ((float)row)*rowRatio;
            double j = Math.floor(srcRow);
            double t = srcRow - j;
            for(int col=0; col<destW; col++) {
                double srcCol = ((float)col)*colRatio;
                double k = Math.floor(srcCol);
                double u = srcCol - k;
                for(int i=0; i<4; i++) {
                    tempPixels[0][0] = getRGBValue(input3DData,j-1, k-1,i);
                    tempPixels[0][1] = getRGBValue(input3DData,j-1, k, i);
                    tempPixels[0][2] = getRGBValue(input3DData, j-1,k+1, i);
                    tempPixels[0][3] = getRGBValue(input3DData, j-1, k+2,i);

                    tempPixels[1][0] = getRGBValue(input3DData, j, k-1, i);
                    tempPixels[1][1] = getRGBValue(input3DData, j, k, i);
                    tempPixels[1][2] = getRGBValue(input3DData, j, k+1, i);
                    tempPixels[1][3] = getRGBValue(input3DData, j, k+2, i);

                    tempPixels[2][0] = getRGBValue(input3DData, j+1,k-1,i);
                    tempPixels[2][1] = getRGBValue(input3DData, j+1, k, i);
                    tempPixels[2][2] = getRGBValue(input3DData, j+1, k+1, i);
                    tempPixels[2][3] = getRGBValue(input3DData, j+1, k+2, i);

                    tempPixels[3][0] = getRGBValue(input3DData, j+2, k-1, i);
                    tempPixels[3][1] = getRGBValue(input3DData, j+2, k, i);
                    tempPixels[3][2] = getRGBValue(input3DData, j+2, k+1, i);
                    tempPixels[3][3] = getRGBValue(input3DData, j+2, k+2, i);

                    // update coefficients
                    updateCoefficients(tempPixels);
                    outputThreeDeminsionData[row][col][i] = getPixelValue(getValue(t, u));
                }

            }
        }

        return convertToOneDim(outputThreeDeminsionData, destW, destH);
    }

    private static double getRGBValue(double[][][] input3DData, double row, double col, int index) {
        if(col >= srcWidth) {
            col = srcWidth - 1;
        }

        if(col < 0) {
            col = 0;
        }

        if(row >= srcHeight) {
            row = srcHeight - 1;
        }

        if(row < 0) {
            row = 0;
        }

        //System.out.println("row:" + (int)row + ", col:" + (int)col + ", index : " + index);
        return input3DData[(int)row][(int)col][index];
    }

    private static double getRGBValue2(double[] inPixelsData, double row, double col) {
        if(col >= srcWidth) {
            col = srcWidth - 1;
        }

        if(col < 0) {
            col = 0;
        }

        if(row >= srcHeight) {
            row = srcHeight - 1;
        }

        if(row < 0) {
            row = 0;
        }

       // System.out.println("row:" + (int)row + ", col:" + (int)col + ", index : " + index);
        return inPixelsData[((int)row)*srcWidth + (int)col];
    }

    private static int getPixelValue(double pixelValue) {
        return pixelValue < 0 ? 0: pixelValue >255.0d ?255:(int)pixelValue;
    }

    private static void updateCoefficients (double[][] p) {
        a00 = p[1][1];
        a01 = -.5*p[1][0] + .5*p[1][2];
        a02 = p[1][0] - 2.5*p[1][1] + 2*p[1][2] - .5*p[1][3];
        a03 = -.5*p[1][0] + 1.5*p[1][1] - 1.5*p[1][2] + .5*p[1][3];
        a10 = -.5*p[0][1] + .5*p[2][1];
        a11 = .25*p[0][0] - .25*p[0][2] - .25*p[2][0] + .25*p[2][2];
        a12 = -.5*p[0][0] + 1.25*p[0][1] - p[0][2] + .25*p[0][3] + .5*p[2][0] - 1.25*p[2][1] + p[2][2] - .25*p[2][3];
        a13 = .25*p[0][0] - .75*p[0][1] + .75*p[0][2] - .25*p[0][3] - .25*p[2][0] + .75*p[2][1] - .75*p[2][2] + .25*p[2][3];
        a20 = p[0][1] - 2.5*p[1][1] + 2*p[2][1] - .5*p[3][1];
        a21 = -.5*p[0][0] + .5*p[0][2] + 1.25*p[1][0] - 1.25*p[1][2] - p[2][0] + p[2][2] + .25*p[3][0] - .25*p[3][2];
        a22 = p[0][0] - 2.5*p[0][1] + 2*p[0][2] - .5*p[0][3] - 2.5*p[1][0] + 6.25*p[1][1] - 5*p[1][2] + 1.25*p[1][3] + 2*p[2][0] - 5*p[2][1] + 4*p[2][2] - p[2][3] - .5*p[3][0] + 1.25*p[3][1] - p[3][2] + .25*p[3][3];
        a23 = -.5*p[0][0] + 1.5*p[0][1] - 1.5*p[0][2] + .5*p[0][3] + 1.25*p[1][0] - 3.75*p[1][1] + 3.75*p[1][2] - 1.25*p[1][3] - p[2][0] + 3*p[2][1] - 3*p[2][2] + p[2][3] + .25*p[3][0] - .75*p[3][1] + .75*p[3][2] - .25*p[3][3];
        a30 = -.5*p[0][1] + 1.5*p[1][1] - 1.5*p[2][1] + .5*p[3][1];
        a31 = .25*p[0][0] - .25*p[0][2] - .75*p[1][0] + .75*p[1][2] + .75*p[2][0] - .75*p[2][2] - .25*p[3][0] + .25*p[3][2];
        a32 = -.5*p[0][0] + 1.25*p[0][1] - p[0][2] + .25*p[0][3] + 1.5*p[1][0] - 3.75*p[1][1] + 3*p[1][2] - .75*p[1][3] - 1.5*p[2][0] + 3.75*p[2][1] - 3*p[2][2] + .75*p[2][3] + .5*p[3][0] - 1.25*p[3][1] + p[3][2] - .25*p[3][3];
        a33 = .25*p[0][0] - .75*p[0][1] + .75*p[0][2] - .25*p[0][3] - .75*p[1][0] + 2.25*p[1][1] - 2.25*p[1][2] + .75*p[1][3] + .75*p[2][0] - 2.25*p[2][1] + 2.25*p[2][2] - .75*p[2][3] - .25*p[3][0] + .75*p[3][1] - .75*p[3][2] + .25*p[3][3];
    }

    private static double getValue (double x, double y) {
        double x2 = x * x;
        double x3 = x2 * x;
        double y2 = y * y;
        double y3 = y2 * y;

        return (a00 + a01 * y + a02 * y2 + a03 * y3) +
                (a10 + a11 * y + a12 * y2 + a13 * y3) * x +
                (a20 + a21 * y + a22 * y2 + a23 * y3) * x2 +
                (a30 + a31 * y + a32 * y2 + a33 * y3) * x3;
    }

    /* <p> The purpose of this method is to convert the data in the 3D array of ints back into </p>
     * <p> the 1d array of type int. </p>
     *
     */
    private static int[] convertToOneDim(int[][][] data, int imgCols, int imgRows) {
        // Create the 1D array of type int to be populated with pixel data
        int[] oneDPix = new int[imgCols * imgRows * 4];

        // Move the data into the 1D array. Note the
        // use of the bitwise OR operator and the
        // bitwise left-shift operators to put the
        // four 8-bit bytes into each int.
        for (int row = 0, cnt = 0; row < imgRows; row++) {
            for (int col = 0; col < imgCols; col++) {
                oneDPix[cnt] = ((data[row][col][0] << 24) & 0xFF000000)
                        | ((data[row][col][1] << 16) & 0x00FF0000)
                        | ((data[row][col][2] << 8) & 0x0000FF00)
                        | ((data[row][col][3]) & 0x000000FF);
                cnt++;
            }// end for loop on col

        }// end for loop on row

        return oneDPix;
    }// end convertToOneDim

    private static double [][][] processOneToThreeDeminsion(int[] oneDPix2, int imgRows, int imgCols) {
        double[][][] tempData = new double[imgRows][imgCols][4];
        for(int row=0; row<imgRows; row++) {

            // per row processing
            int[] aRow = new int[imgCols];
            for (int col = 0; col < imgCols; col++) {
                int element = row * imgCols + col;
                aRow[col] = oneDPix2[element];
            }

            // convert to three dimension data
            for(int col=0; col<imgCols; col++) {
                tempData[row][col][0] = (aRow[col] >> 24) & 0xFF; // alpha
                tempData[row][col][1] = (aRow[col] >> 16) & 0xFF; // red
                tempData[row][col][2] = (aRow[col] >> 8) & 0xFF;  // green
                tempData[row][col][3] = (aRow[col]) & 0xFF;       // blue
            }
        }
        return tempData;
    }

    public static void main(String args[]) {
        double[] doubleArr = {2.5d, 4.9d, 3.4d,5.3d,2.3d,3.9d,3,8d,12.4d};
        double[] result = imgScale2(doubleArr, 1, doubleArr.length, 1, doubleArr.length * 9);
        System.out.println("---> output size: " + result == null ? 0 : result.length);

      /*  for (int i = 0; i < result.length; i++)
            for (int j = 0; j < result[i].length; j++)
                for (int k = 0; k < result[i][j].length; k++)
                    System.out.println(result[i][j][k]);*/
    }


    public static double[] imgScale2(double[] inPixelsData, int srcW, int srcH, int destW, int destH) {
        //double[][][] input3DData = processOneToThreeDeminsion(inPixelsData, srcH, srcW); //图片 -> 4个二维数组 -> argb
        double[][][] input3DData = new double[inPixelsData.length][1][1];
      //  input3DData[0][0] = inPixelsData;

     /*   for (int i = 0; i < input3DData.length; i++)
            for (int j = 0; j < input3DData[i].length; j++)
                input3DData[i][j][0] = inPixelsData[j];*/

        for(int i = 0; i < inPixelsData.length; i++){
            input3DData[i][0][0] = inPixelsData[i];
        }
        int[][][] outputThreeDeminsionData = new int[destH][destW][1];
        double[] result = new double[outputThreeDeminsionData.length];
        double[][] tempPixels = new double[4][4];
        float rowRatio = ((float)srcH)/((float)destH); //1/3
        float colRatio = ((float)srcW)/((float)destW);
        srcWidth = srcW;
        srcHeight = srcH;
        int index = 0;
        for(int row=0; row<destH; row++) {
            // convert to three dimension data
            double srcRow = ((float)row)*rowRatio;
            double j = Math.floor(srcRow);
            double t = srcRow - j;
            for(int col=0; col<destW; col++) {
                double srcCol = ((float)col)*colRatio;
                double k = Math.floor(srcCol);
                double u = srcCol - k;
                for(int i=0; i<1; i++) {
                  /*  tempPixels[0][0] = getRGBValue(input3DData,j-1, k-1,i);
                    tempPixels[0][1] = getRGBValue(input3DData,j-1, k, i);
                    tempPixels[0][2] = getRGBValue(input3DData, j-1,k+1, i);
                    tempPixels[0][3] = getRGBValue(input3DData, j-1, k+2,i);

                    tempPixels[1][0] = getRGBValue(input3DData, j, k-1, i);
                    tempPixels[1][1] = getRGBValue(input3DData, j, k, i);
                    tempPixels[1][2] = getRGBValue(input3DData, j, k+1, i);
                    tempPixels[1][3] = getRGBValue(input3DData, j, k+2, i);

                    tempPixels[2][0] = getRGBValue(input3DData, j+1,k-1,i);
                    tempPixels[2][1] = getRGBValue(input3DData, j+1, k, i);
                    tempPixels[2][2] = getRGBValue(input3DData, j+1, k+1, i);
                    tempPixels[2][3] = getRGBValue(input3DData, j+1, k+2, i);

                    tempPixels[3][0] = getRGBValue(input3DData, j+2, k-1, i);
                    tempPixels[3][1] = getRGBValue(input3DData, j+2, k, i);
                    tempPixels[3][2] = getRGBValue(input3DData, j+2, k+1, i);
                    tempPixels[3][3] = getRGBValue(input3DData, j+2, k+2, i);


*/
                /*    tempPixels[0][0] = getRGBValue(input3DData,j-8, k,i);
                    tempPixels[0][1] = getRGBValue(input3DData,j-7, k, i);
                    tempPixels[0][2] = getRGBValue(input3DData, j-6,k, i);
                    tempPixels[0][3] = getRGBValue(input3DData, j-5, k,i);

                    tempPixels[1][0] = getRGBValue(input3DData, j-4, k, i);
                    tempPixels[1][1] = getRGBValue(input3DData, j-3, k, i);
                    tempPixels[1][2] = getRGBValue(input3DData, j-2, k, i);
                    tempPixels[1][3] = getRGBValue(input3DData, j-1, k, i);

                    tempPixels[2][0] = getRGBValue(input3DData, j+0,k,i);
                    tempPixels[2][1] = getRGBValue(input3DData, j+1, k, i);
                    tempPixels[2][2] = getRGBValue(input3DData, j+2, k, i);
                    tempPixels[2][3] = getRGBValue(input3DData, j+3, k, i);

                    tempPixels[3][0] = getRGBValue(input3DData, j+4, k, i);
                    tempPixels[3][1] = getRGBValue(input3DData, j+5, k, i);
                    tempPixels[3][2] = getRGBValue(input3DData, j+6, k, i);
                    tempPixels[3][3] = getRGBValue(input3DData, j+7, k, i);*/

                    tempPixels[0][0] = getRGBValue(input3DData, j, k, i);
                    tempPixels[0][1] = getRGBValue(input3DData, j, k, i);
                    tempPixels[0][2] = getRGBValue(input3DData, j, k, i);
                    tempPixels[0][3] = getRGBValue(input3DData, j, k, i);

                    tempPixels[1][0] = getRGBValue(input3DData, j, k, i);
                    tempPixels[1][1] = getRGBValue(input3DData, j, k, i);
                    tempPixels[1][2] = getRGBValue(input3DData, j, k, i);
                    tempPixels[1][3] = getRGBValue(input3DData, j, k, i);

                    tempPixels[2][0] = getRGBValue(input3DData, j,k,  i);
                    tempPixels[2][1] = getRGBValue(input3DData, j, k, i);
                    tempPixels[2][2] = getRGBValue(input3DData, j, k, i);
                    tempPixels[2][3] = getRGBValue(input3DData, j, k, i);

                    tempPixels[3][0] = getRGBValue(input3DData, j, k, i);
                    tempPixels[3][1] = getRGBValue(input3DData, j, k, i);
                    tempPixels[3][2] = getRGBValue(input3DData, j, k, i);
                    tempPixels[3][3] = getRGBValue(input3DData, j, k, i);



                    // update coefficients
                    updateCoefficients(tempPixels);
                    //outputThreeDeminsionData[row][col][i] = getPixelValue(getValue(t, u));
                    double val = getValue(t, u);
                    outputThreeDeminsionData[row][col][i] = (int)val;
                    if (index < result.length) {
                        result[index] = val;
                        index++;
                    }

                }

            }
        }

       // return convertToOneDim(outputThreeDeminsionData, destW, destH);
        return result;
    }

    //二维数组转换 2层for循环 效率一般 即使for循环什么都不做 空跑一次需要1.5s左右
    public static double[] imgScale3(double[] inPixelsData, int srcW, int srcH, int destW, int destH) {
        //double[][][] input3DData = processOneToThreeDeminsion(inPixelsData, srcH, srcW); //图片 -> 4个二维数组 -> argb
       //double[][][] input3DData = new double[srcH][srcW][1];

        //  input3DData[0][0] = inPixelsData;

        /*for (int i = 0; i < input3DData.length -1 ; i++){
            for (int j = 0; j < input3DData[i].length -1 ; j++){
                System.out.println("----> i=" + i + ", j=" + j + ", result:" +  (input3DData.length * i + j)
                        + " widthLength:" + (input3DData.length -1) + ", heightLength:" + (input3DData[i].length -1));
                input3DData[i][j][0] = inPixelsData[i + j];
            }
        }*/

      /*  for (int i = 0; i < srcH ; i++){
            for (int j = 0; j < srcW ; j++){
                //System.out.println("----> i=" + i + ", j=" + j + ", result:" +  inPixelsData[i*srcH + j]);
                input3DData[i][j][0] = inPixelsData[i*srcW + j];
            }
        }*/



      /*  for(int i = 0; i < inPixelsData.length; i++){
            input3DData[i][0][0] = inPixelsData[i];
        }*/
        int[][][] outputThreeDeminsionData = new int[destH][destW][1];
        double[] result = new double[destH * destW];
        double[][] tempPixels = new double[4][4];
        float rowRatio = ((float)srcH)/((float)destH); //1/3
        float colRatio = ((float)srcW)/((float)destW);
        srcWidth = srcW;
        srcHeight = srcH;
        long rowStart = System.currentTimeMillis();
        for(int row=0; row<destH; row++) {
            // convert to three dimension data
            double srcRow = ((float)row)*rowRatio;
            double j = Math.floor(srcRow);
            double t = srcRow - j;
            long singleRowStart = System.currentTimeMillis();
            for(int col=0; col<destW; col++) {
                double srcCol = ((float)col)*colRatio;
                double k = Math.floor(srcCol);
                double u = srcCol - k;
               // for(int i=0; i<1; i++) {
                    long getValueStart = System.currentTimeMillis();

                    tempPixels[0][0] = getRGBValue2(inPixelsData,j-1, k-1);
                    tempPixels[0][1] = getRGBValue2(inPixelsData,j-1, k);
                    tempPixels[0][2] = getRGBValue2(inPixelsData, j-1,k+1);
                    tempPixels[0][3] = getRGBValue2(inPixelsData, j-1, k+2);

                    tempPixels[1][0] = getRGBValue2(inPixelsData, j, k-1);
                    tempPixels[1][1] = getRGBValue2(inPixelsData, j, k);
                    tempPixels[1][2] = getRGBValue2(inPixelsData, j, k+1);
                    tempPixels[1][3] = getRGBValue2(inPixelsData, j, k+2);

                    tempPixels[2][0] = getRGBValue2(inPixelsData, j+1,k-1);
                    tempPixels[2][1] = getRGBValue2(inPixelsData, j+1, k);
                    tempPixels[2][2] = getRGBValue2(inPixelsData, j+1, k+1);
                    tempPixels[2][3] = getRGBValue2(inPixelsData, j+1, k+2);

                    tempPixels[3][0] = getRGBValue2(inPixelsData, j+2, k-1);
                    tempPixels[3][1] = getRGBValue2(inPixelsData, j+2, k);
                    tempPixels[3][2] = getRGBValue2(inPixelsData, j+2, k+1);
                    tempPixels[3][3] = getRGBValue2(inPixelsData, j+2, k+2);



                /*    tempPixels[0][0] = getRGBValue(input3DData,j-8, k,i);
                    tempPixels[0][1] = getRGBValue(input3DData,j-7, k, i);
                    tempPixels[0][2] = getRGBValue(input3DData, j-6,k, i);
                    tempPixels[0][3] = getRGBValue(input3DData, j-5, k,i);

                    tempPixels[1][0] = getRGBValue(input3DData, j-4, k, i);
                    tempPixels[1][1] = getRGBValue(input3DData, j-3, k, i);
                    tempPixels[1][2] = getRGBValue(input3DData, j-2, k, i);
                    tempPixels[1][3] = getRGBValue(input3DData, j-1, k, i);

                    tempPixels[2][0] = getRGBValue(input3DData, j+0,k,i);
                    tempPixels[2][1] = getRGBValue(input3DData, j+1, k, i);
                    tempPixels[2][2] = getRGBValue(input3DData, j+2, k, i);
                    tempPixels[2][3] = getRGBValue(input3DData, j+3, k, i);

                    tempPixels[3][0] = getRGBValue(input3DData, j+4, k, i);
                    tempPixels[3][1] = getRGBValue(input3DData, j+5, k, i);
                    tempPixels[3][2] = getRGBValue(input3DData, j+6, k, i);
                    tempPixels[3][3] = getRGBValue(input3DData, j+7, k, i);*/

                    // update coefficients
                    //updateCoefficients(tempPixels);
                    //outputThreeDeminsionData[row][col][i] = getPixelValue(getValue(t, u));
                  //  double val = getValue(t, u);
                    double val = inPixelsData[((int)j)*srcWidth + (int)k];
                    outputThreeDeminsionData[row][col][0] = (int)val;
                    result[row*destW + col] = val;

                long getValueEnd = System.currentTimeMillis();
               // System.out.println(" -------------- getValueTime : " + (getValueEnd - getValueStart));
              //  }

            }
            long singleRowEnd = System.currentTimeMillis();
          //  System.out.println(" -------------- SingleRowTime : " + (singleRowEnd - singleRowStart));
        }

        long rowEnd = System.currentTimeMillis();
        System.out.println(" -------------- ----------------------------------------------------------------> UseTime : " + (rowEnd - rowStart));
        // return convertToOneDim(outputThreeDeminsionData, destW, destH);
        return result;
    }

    public static double[] imgScale_oneArray(double[] inPixelsData, int srcW, int srcH, int destW, int destH) {
      /*  for(int i = 0; i < inPixelsData.length; i++){
            input3DData[i][0][0] = inPixelsData[i];
        }*/
        double[][] tempPixels = new double[4][4];
        double[] result = new double[destH * destW];
        float rowRatio = ((float)srcH)/((float)destH); //1/3
        float colRatio = ((float)srcW)/((float)destW);
        srcWidth = srcW;
        srcHeight = srcH;
        long rowStart = System.currentTimeMillis();

        for(int i=0;i<result.length;i++){
            int x = i / destW;
            int y = i % destW;

            //缩放
            double ori_x = x * rowRatio;
            double j = Math.floor(ori_x);
            double t = ori_x - j; //小数部分

            double ori_y = y * colRatio;
            double k = Math.floor(ori_y);
            double u = ori_y - k;

            tempPixels[0][0] = getRGBValue2(inPixelsData,j-1, k-1);
            tempPixels[0][1] = getRGBValue2(inPixelsData,j-1, k);
            tempPixels[0][2] = getRGBValue2(inPixelsData, j-1,k+1);
            tempPixels[0][3] = getRGBValue2(inPixelsData, j-1, k+2);

            tempPixels[1][0] = getRGBValue2(inPixelsData, j, k-1);
            tempPixels[1][1] = getRGBValue2(inPixelsData, j, k);
            tempPixels[1][2] = getRGBValue2(inPixelsData, j, k+1);
            tempPixels[1][3] = getRGBValue2(inPixelsData, j, k+2);

            tempPixels[2][0] = getRGBValue2(inPixelsData, j+1,k-1);
            tempPixels[2][1] = getRGBValue2(inPixelsData, j+1, k);
            tempPixels[2][2] = getRGBValue2(inPixelsData, j+1, k+1);
            tempPixels[2][3] = getRGBValue2(inPixelsData, j+1, k+2);

            tempPixels[3][0] = getRGBValue2(inPixelsData, j+2, k-1);
            tempPixels[3][1] = getRGBValue2(inPixelsData, j+2, k);
            tempPixels[3][2] = getRGBValue2(inPixelsData, j+2, k+1);
            tempPixels[3][3] = getRGBValue2(inPixelsData, j+2, k+2);

            updateCoefficients(tempPixels);
            //double val = inPixelsData[((int)j)*srcWidth + (int)k];
            double val = getValue(t, u);
           // result[row*destW + col] = val;
            result[i] = val;
        }

        long rowEnd = System.currentTimeMillis();
        System.out.println(" -------------- ----------------------------------------------------------------> UseTime : " + (rowEnd - rowStart));
        // return convertToOneDim(outputThreeDeminsionData, destW, destH);
        return result;
    }

}

