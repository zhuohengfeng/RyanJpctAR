package com.ryan.ryanjpctar.vuforia.utils;

import com.vuforia.Matrix44F;

public class SampleMath {

    private static float temp[] = new float[16];

    public static Matrix44F Matrix44FTranspose(Matrix44F m) {
        Matrix44F r = new Matrix44F();
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                temp[i * 4 + j] = m.getData()[i + 4 * j];

        r.setData(temp);
        return r;
    }

    private static float Matrix44FDeterminate(Matrix44F m) {
        return m.getData()[12] * m.getData()[9] * m.getData()[6]
                * m.getData()[3] - m.getData()[8] * m.getData()[13]
                * m.getData()[6] * m.getData()[3] - m.getData()[12]
                * m.getData()[5] * m.getData()[10] * m.getData()[3]
                + m.getData()[4] * m.getData()[13] * m.getData()[10]
                * m.getData()[3] + m.getData()[8] * m.getData()[5]
                * m.getData()[14] * m.getData()[3] - m.getData()[4]
                * m.getData()[9] * m.getData()[14] * m.getData()[3]
                - m.getData()[12] * m.getData()[9] * m.getData()[2]
                * m.getData()[7] + m.getData()[8] * m.getData()[13]
                * m.getData()[2] * m.getData()[7] + m.getData()[12]
                * m.getData()[1] * m.getData()[10] * m.getData()[7]
                - m.getData()[0] * m.getData()[13] * m.getData()[10]
                * m.getData()[7] - m.getData()[8] * m.getData()[1]
                * m.getData()[14] * m.getData()[7] + m.getData()[0]
                * m.getData()[9] * m.getData()[14] * m.getData()[7]
                + m.getData()[12] * m.getData()[5] * m.getData()[2]
                * m.getData()[11] - m.getData()[4] * m.getData()[13]
                * m.getData()[2] * m.getData()[11] - m.getData()[12]
                * m.getData()[1] * m.getData()[6] * m.getData()[11]
                + m.getData()[0] * m.getData()[13] * m.getData()[6]
                * m.getData()[11] + m.getData()[4] * m.getData()[1]
                * m.getData()[14] * m.getData()[11] - m.getData()[0]
                * m.getData()[5] * m.getData()[14] * m.getData()[11]
                - m.getData()[8] * m.getData()[5] * m.getData()[2]
                * m.getData()[15] + m.getData()[4] * m.getData()[9]
                * m.getData()[2] * m.getData()[15] + m.getData()[8]
                * m.getData()[1] * m.getData()[6] * m.getData()[15]
                - m.getData()[0] * m.getData()[9] * m.getData()[6]
                * m.getData()[15] - m.getData()[4] * m.getData()[1]
                * m.getData()[10] * m.getData()[15] + m.getData()[0]
                * m.getData()[5] * m.getData()[10] * m.getData()[15];
    }


    public static Matrix44F Matrix44FInverse(Matrix44F m) {
        Matrix44F r = new Matrix44F();

        float det = 1.0f / Matrix44FDeterminate(m);

        temp[0] = m.getData()[6] * m.getData()[11] * m.getData()[13]
                - m.getData()[7] * m.getData()[10] * m.getData()[13]
                + m.getData()[7] * m.getData()[9] * m.getData()[14]
                - m.getData()[5] * m.getData()[11] * m.getData()[14]
                - m.getData()[6] * m.getData()[9] * m.getData()[15]
                + m.getData()[5] * m.getData()[10] * m.getData()[15];

        temp[4] = m.getData()[3] * m.getData()[10] * m.getData()[13]
                - m.getData()[2] * m.getData()[11] * m.getData()[13]
                - m.getData()[3] * m.getData()[9] * m.getData()[14]
                + m.getData()[1] * m.getData()[11] * m.getData()[14]
                + m.getData()[2] * m.getData()[9] * m.getData()[15]
                - m.getData()[1] * m.getData()[10] * m.getData()[15];

        temp[8] = m.getData()[2] * m.getData()[7] * m.getData()[13]
                - m.getData()[3] * m.getData()[6] * m.getData()[13]
                + m.getData()[3] * m.getData()[5] * m.getData()[14]
                - m.getData()[1] * m.getData()[7] * m.getData()[14]
                - m.getData()[2] * m.getData()[5] * m.getData()[15]
                + m.getData()[1] * m.getData()[6] * m.getData()[15];

        temp[12] = m.getData()[3] * m.getData()[6] * m.getData()[9]
                - m.getData()[2] * m.getData()[7] * m.getData()[9] - m.getData()[3]
                * m.getData()[5] * m.getData()[10] + m.getData()[1]
                * m.getData()[7] * m.getData()[10] + m.getData()[2]
                * m.getData()[5] * m.getData()[11] - m.getData()[1]
                * m.getData()[6] * m.getData()[11];

        temp[1] = m.getData()[7] * m.getData()[10] * m.getData()[12]
                - m.getData()[6] * m.getData()[11] * m.getData()[12]
                - m.getData()[7] * m.getData()[8] * m.getData()[14]
                + m.getData()[4] * m.getData()[11] * m.getData()[14]
                + m.getData()[6] * m.getData()[8] * m.getData()[15]
                - m.getData()[4] * m.getData()[10] * m.getData()[15];

        temp[5] = m.getData()[2] * m.getData()[11] * m.getData()[12]
                - m.getData()[3] * m.getData()[10] * m.getData()[12]
                + m.getData()[3] * m.getData()[8] * m.getData()[14]
                - m.getData()[0] * m.getData()[11] * m.getData()[14]
                - m.getData()[2] * m.getData()[8] * m.getData()[15]
                + m.getData()[0] * m.getData()[10] * m.getData()[15];

        temp[9] = m.getData()[3] * m.getData()[6] * m.getData()[12]
                - m.getData()[2] * m.getData()[7] * m.getData()[12]
                - m.getData()[3] * m.getData()[4] * m.getData()[14]
                + m.getData()[0] * m.getData()[7] * m.getData()[14]
                + m.getData()[2] * m.getData()[4] * m.getData()[15]
                - m.getData()[0] * m.getData()[6] * m.getData()[15];

        temp[13] = m.getData()[2] * m.getData()[7] * m.getData()[8]
                - m.getData()[3] * m.getData()[6] * m.getData()[8] + m.getData()[3]
                * m.getData()[4] * m.getData()[10] - m.getData()[0]
                * m.getData()[7] * m.getData()[10] - m.getData()[2]
                * m.getData()[4] * m.getData()[11] + m.getData()[0]
                * m.getData()[6] * m.getData()[11];

        temp[2] = m.getData()[5] * m.getData()[11] * m.getData()[12]
                - m.getData()[7] * m.getData()[9] * m.getData()[12]
                + m.getData()[7] * m.getData()[8] * m.getData()[13]
                - m.getData()[4] * m.getData()[11] * m.getData()[13]
                - m.getData()[5] * m.getData()[8] * m.getData()[15]
                + m.getData()[4] * m.getData()[9] * m.getData()[15];

        temp[6] = m.getData()[3] * m.getData()[9] * m.getData()[12]
                - m.getData()[1] * m.getData()[11] * m.getData()[12]
                - m.getData()[3] * m.getData()[8] * m.getData()[13]
                + m.getData()[0] * m.getData()[11] * m.getData()[13]
                + m.getData()[1] * m.getData()[8] * m.getData()[15]
                - m.getData()[0] * m.getData()[9] * m.getData()[15];

        temp[10] = m.getData()[1] * m.getData()[7] * m.getData()[12]
                - m.getData()[3] * m.getData()[5] * m.getData()[12]
                + m.getData()[3] * m.getData()[4] * m.getData()[13]
                - m.getData()[0] * m.getData()[7] * m.getData()[13]
                - m.getData()[1] * m.getData()[4] * m.getData()[15]
                + m.getData()[0] * m.getData()[5] * m.getData()[15];

        temp[14] = m.getData()[3] * m.getData()[5] * m.getData()[8]
                - m.getData()[1] * m.getData()[7] * m.getData()[8] - m.getData()[3]
                * m.getData()[4] * m.getData()[9] + m.getData()[0] * m.getData()[7]
                * m.getData()[9] + m.getData()[1] * m.getData()[4]
                * m.getData()[11] - m.getData()[0] * m.getData()[5]
                * m.getData()[11];

        temp[3] = m.getData()[6] * m.getData()[9] * m.getData()[12]
                - m.getData()[5] * m.getData()[10] * m.getData()[12]
                - m.getData()[6] * m.getData()[8] * m.getData()[13]
                + m.getData()[4] * m.getData()[10] * m.getData()[13]
                + m.getData()[5] * m.getData()[8] * m.getData()[14]
                - m.getData()[4] * m.getData()[9] * m.getData()[14];

        temp[7] = m.getData()[1] * m.getData()[10] * m.getData()[12]
                - m.getData()[2] * m.getData()[9] * m.getData()[12]
                + m.getData()[2] * m.getData()[8] * m.getData()[13]
                - m.getData()[0] * m.getData()[10] * m.getData()[13]
                - m.getData()[1] * m.getData()[8] * m.getData()[14]
                + m.getData()[0] * m.getData()[9] * m.getData()[14];

        temp[11] = m.getData()[2] * m.getData()[5] * m.getData()[12]
                - m.getData()[1] * m.getData()[6] * m.getData()[12]
                - m.getData()[2] * m.getData()[4] * m.getData()[13]
                + m.getData()[0] * m.getData()[6] * m.getData()[13]
                + m.getData()[1] * m.getData()[4] * m.getData()[14]
                - m.getData()[0] * m.getData()[5] * m.getData()[14];

        temp[15] = m.getData()[1] * m.getData()[6] * m.getData()[8]
                - m.getData()[2] * m.getData()[5] * m.getData()[8] + m.getData()[2]
                * m.getData()[4] * m.getData()[9] - m.getData()[0] * m.getData()[6]
                * m.getData()[9] - m.getData()[1] * m.getData()[4]
                * m.getData()[10] + m.getData()[0] * m.getData()[5]
                * m.getData()[10];

        for (int i = 0; i < 16; i++)
            temp[i] *= det;

        r.setData(temp);
        return r;
    }


}
