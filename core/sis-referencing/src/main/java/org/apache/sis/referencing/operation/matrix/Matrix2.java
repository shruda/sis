/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.referencing.operation.matrix;

import org.opengis.referencing.operation.Matrix;
import org.apache.sis.internal.util.Numerics;


/**
 * A matrix of fixed {@value #SIZE}&times;{@value #SIZE} size.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.4 (derived from geotk-2.2)
 * @version 0.4
 * @module
 */
final class Matrix2 extends MatrixSIS {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 7116561372481474290L;

    /**
     * The matrix size, which is {@value}.
     */
    public static final int SIZE = 2;

    /** The first matrix element in the first row.   */ private double m00;
    /** The second matrix element in the first row.  */ private double m01;
    /** The first matrix element in the second row.  */ private double m10;
    /** The second matrix element in the second row. */ private double m11;

    /**
     * Creates a new identity matrix.
     */
    public Matrix2() {
        m00 = m11 = 1;
    }

    /**
     * Creates a new matrix initialized to the specified values.
     *
     * @param m00 The first matrix element in the first row.
     * @param m01 The second matrix element in the first row.
     * @param m10 The first matrix element in the second row.
     * @param m11 The second matrix element in the second row.
     */
    public Matrix2(final double m00, final double m01,
                   final double m10, final double m11)
    {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
    }

    /**
     * Creates a new matrix initialized to the specified values.
     * The length of the given array must be 4 and the values in the same order than the above constructor.
     * The array length is not verified by this constructor, since it shall be verified by {@link Matrices}.
     *
     * @param elements Elements of the matrix. Column indices vary fastest.
     */
    Matrix2(final double[] elements) {
        m00 = elements[0];
        m01 = elements[1];
        m10 = elements[2];
        m11 = elements[3];
    }

    /**
     * Creates a new matrix initialized to the same value than the specified one.
     * The specified matrix size must be {@value #SIZE}×{@value #SIZE}.
     * This is not verified by this constructor, since it shall be verified by {@link Matrices}.
     *
     * @param  matrix The matrix to copy.
     */
    Matrix2(final Matrix matrix) {
        m00 = matrix.getElement(0,0);
        m01 = matrix.getElement(0,1);
        m10 = matrix.getElement(1,0);
        m11 = matrix.getElement(1,1);
    }

    /**
     * Returns the number of rows in this matrix, which is always {@value #SIZE} in this implementation.
     */
    @Override
    public final int getNumRow() {
        return SIZE;
    }

    /**
     * Returns the number of columns in this matrix, which is always {@value #SIZE} in this implementation.
     */
    @Override
    public final int getNumCol() {
        return SIZE;
    }

    /**
     * Retrieves the value at the specified row and column of this matrix.
     */
    @Override
    public double getElement(final int row, final int column) {
        switch (row) {
            case 0: {
                switch (column) {
                    case 0: return m00;
                    case 1: return m01;
                }
                break;
            }
            case 1: {
                switch (column) {
                    case 0: return m10;
                    case 1: return m11;
                }
                break;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Modifies the value at the specified row and column of this matrix.
     */
    @Override
    public void setElement(final int row, final int column, final double value) {
        switch (row) {
            case 0: {
                switch (column) {
                    case 0: m00 = value; return;
                    case 1: m01 = value; return;
                }
                break;
            }
            case 1: {
                switch (column) {
                    case 0: m10 = value; return;
                    case 1: m11 = value; return;
                }
                break;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAffine() {
        return m10 == 0 && m11 == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIdentity() {
        return m00 == 1 && m10 == 0 &&
               m01 == 0 && m11 == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIdentity(final double tolerance) {
        return Matrices.isIdentity(this, tolerance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToIdentity() {
        m01 = m10 = 0;
        m00 = m11 = 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToZero() {
        m00 = m01 = m10 = m11 = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void negate() {
        m00 = -m00;
        m01 = -m01;
        m10 = -m10;
        m11 = -m11;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transpose() {
        final double swap = m10;
        m10 = m01;
        m01 = swap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void normalizeColumns() {
        double m;
        m = Math.hypot(m00, m10); m00 /= m; m10 /= m;
        m = Math.hypot(m01, m11); m01 /= m; m11 /= m;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatrixSIS inverse() throws SingularMatrixException {
        final double det = m00*m11 - m01*m10;
        if (det == 0) {
            throw new SingularMatrixException();
        }
        return new Matrix2(m11 / det, -m01 / det,
                          -m10 / det,  m00 / det);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatrixSIS multiply(final Matrix matrix) {
        final Matrix2 k;
        if (matrix instanceof Matrix2) {
            k = (Matrix2) matrix;
        } else {
            ensureSizeMatch(SIZE, matrix);
            k = new Matrix2(matrix);
        }
        return new Matrix2(m00*k.m00 + m01*k.m10,
                           m00*k.m01 + m01*k.m11,
                           m10*k.m00 + m11*k.m10,
                           m10*k.m01 + m11*k.m11);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Matrix matrix, final double tolerance) {
        return Matrices.equals(this, matrix, tolerance, false);
    }

    /**
     * Returns {@code true} if the specified object is of type {@code Matrix2} and
     * all of the data members are equal to the corresponding data members in this matrix.
     *
     * @param object The object to compare with this matrix for equality.
     * @return {@code true} if the given object is equal to this matrix.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof Matrix2) {
            final Matrix2 that = (Matrix2) object;
            return Numerics.equals(this.m00, that.m00) &&
                   Numerics.equals(this.m01, that.m01) &&
                   Numerics.equals(this.m10, that.m10) &&
                   Numerics.equals(this.m11, that.m11);
        }
        return false;
    }

    /**
     * Returns a hash code value based on the data values in this object.
     */
    @Override
    public int hashCode() {
        final long code = serialVersionUID ^
                (((Double.doubleToLongBits(m00)  +
              31 * Double.doubleToLongBits(m01)) +
              31 * Double.doubleToLongBits(m10)) +
              31 * Double.doubleToLongBits(m11));
        return ((int) code) ^ ((int) (code >>> 32));
    }
}
