package util.linearalgebra;

/**
 *
 * @author Melroy
 */
public class Matrix {
    //The value within the matrix.
    private final double[][] values;
    
    public final int n;
    public final int m;

    /**
     * Create a matrix from a given double double array.
     * 
     * @param values The values to base the matrix on.
     */
    public Matrix(double[][] values) {
        this.values = values;
        n = values.length;
        m = values[0].length;
    }
    
    /**
     * Create a n x m matrix filled with the given value.
     * 
     * @param value The value to fill the matrix with.
     * @param n The amount of rows in the matrix.
     * @param m The amount of columns in the matrix.
     */
    public Matrix(double value, int n, int m) {
        values = new double[n][m];
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                values[i][j] = value;
            }
        }
        this.n = n;
        this.m = m;
    }
    
    /**
     * Add the given matrix to this matrix.
     * 
     * @param m1 The matrix to add to this matrix.
     * @return A new matrix that is the addition of this matrix and the other matrix.
     */
    public Matrix add(Matrix m1) {
        checkDimensions(this, m1);
        double[][] values = new double[n][m];

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                values[i][j] = this.values[i][j] + m1.values[i][j];
            }
        }
        return new Matrix(values);
    } 
    
    /**
     * Subtract the given matrix from this matrix.
     * 
     * @param m1 The matrix to subtract from this matrix.
     * @return A new matrix that is the addition of this matrix and the other matrix.
     */
    public Matrix subtract(Matrix m1) {
        checkDimensions(this, m1);
        double[][] values = new double[n][m];

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                values[i][j] = this.values[i][j] - m1.values[i][j];
            }
        }
        return new Matrix(values);
    } 
    
    /**
     * Scale the vector by the given scalar.
     * 
     * @param scalar The value to multiply the vector by.
     * @return A new matrix that is the addition of this matrix and the other matrix.
     */
    public Matrix scale(double scalar) {
        double[][] values = new double[n][m];

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                values[i][j] = this.values[i][j] * scalar;
            }
        }
        return new Matrix(values);
    } 
    
    /**
     * Multiply the matrix by a column vector (matrix-vector product).
     * 
     * @param v1 The matrix to multiply by.
     * @return A vector that is the result of the multiplication.
     */
    public Vector multiply(Vector v1) {
        checkDimensions(this, v1);
        double[] vector = new double[n];
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                vector[i] += values[i][j] * v1.getValues()[j];
            }
        } 
        return new Vector(vector);
    }
    
    /**
     * Multiply the matrix by another matrix (matrix-matrix product).
     * 
     * @param m1 The matrix to multiply by.
     * @return A matrix that is the result of the multiplication.
     */
    public Matrix multiply(Matrix m1) {
        if(this.n != m1.m || this.m != m1.n) {
            throw new IllegalArgumentException("The matrices cannot be multiplied because of their deviating size! m1.n == m2.m && m1.m == m2.n is required!");
        }
        
        double[][] matrix = new double[n][n];
        //Iterate over the rows of the first matrix.
        for(int i = 0; i < n; i++) {
            //Iterate over the columns of the second matrix, which is essentially the same as the loop above.
            for(int j = 0; j < n; j++) {
                //Take the dot product of the ith row in matrix 1, and the jth column in matrix 2.
                //These should both have length m, else we throw an exception above.
                
                for(int k = 0; k < m; k++) {
                    matrix[i][j] += values[i][k] * m1.values[k][j];
                }
            }
        } 
        return new Matrix(matrix);
    }
    
    /**
     * Get the determinant of this matrix.
     * 
     * @return The determinant value, if it exists.
     * @throws util.linearalgebra.NoSquareException When the given matrix is not a square matrix.
     */
    public double getDeterminant() throws NoSquareException {
        return getDeterminant(values, n, m);
    }
    
    /**
     * Get the determinant of the given two dimensional array.
     * 
     * @param m The two dimensional array.
     * @param n The dimensions of the two dimensional array (rows).
     * @param n The dimensions of the two dimensional array (columns).
     * @throws util.linearalgebra.NoSquareException When the given matrix is not a square matrix.
     * @return The determinant value, if it exists.
     */
    private static double getDeterminant(double matrix[][], int n, int m) throws NoSquareException {
        if(n != m) {
            throw new NoSquareException("The determinant needs a square matrix!");
        }
        
        switch (n) {
            case 1:
                return matrix[0][0];
            case 2:
                return matrix[0][0] * matrix[1][1] - matrix[1][0] * matrix[0][1];
            default:
                double result = 0;
                
                for(int k = 0; k < n; k++) {
                    double[][] sub = new double[n-1][n-1];
                    for(int i = 1; i < n; i++) {
                        int pointer = 0;
                        for(int j = 0; j < n; j++) {
                            if(j == k) continue;
                            
                            sub[i-1][pointer] = matrix[i][j];
                            pointer++;
                        }
                    }
                    
                    //Use the power method to change the sign. Even = 1 and odd = -1.
                    result += getSign(k) * matrix[0][k] * getDeterminant(sub, n-1, n-1);
                }
                
                return result;
        }
    }

    /**
     * Get the co-factor of the matrix.
     *
     * @return The co factor of the matrix in matrix form.
     * @throws NoSquareException When the matrix is not in a square form.
     */
    public Matrix getCoFactor() throws NoSquareException {
        double[][] matrix = new double[n][m];
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                matrix[i][j] = getSign(i) * getSign(j) * getDeterminant(getSubArray(values, i, j), n - 1, m - 1);
            }
        } 
        return new Matrix(matrix);
    }

    /**
     * Get the inverse of the matrix.
     *
     * @return The inverse of the matrix.
     * @throws NoSquareException When the matrix is not square.
     */
    public Matrix getInverse() throws NoSquareException {
        Matrix inverse = (this.getCoFactor()).getTransposeMatrix();
        inverse = inverse.scale(1.0/this.getDeterminant());
        return inverse;
    }

    /**
     * Get the appropriate sign for the i-th step of the calculation.
     * @param i The index.
     * @return 1 if even, -1 when i is odd.
     */
    private static int getSign(int i) {
        if (i%2==0) {
            return 1;
        }
        return -1;
    }

    /**
     * Get the sub matrix in double[][] form with exclude_row and exclude_column removed.
     * @param source The source matrix in double[][] form.
     * @param exclude_row The row id to remove.
     * @param exclude_column The column id to remove.
     * @return The sub matrix in double[][] form with exclude_row and exclude_column removed.
     */
    private static double[][] getSubArray(double[][] source, int exclude_row, int exclude_column) {
        int n = source.length;
        int m = source[0].length;
        double[][] result = new double[n - 1][m - 1];
        int current_row = 0;
        for(int i = 0; i < n; i++) {
            if(i == exclude_row) {
                continue;
            }
            
            int current_column = 0;
            for(int j = 0; j < m; j++) {
                if(j == exclude_column) {
                    continue;
                }
                
                result[current_row][current_column] = source[i][j]; 
                
                current_column++;
            }
            
            current_row++;
        } 
        return result;
    }
    
    /**
     * Get the transpose of this matrix.
     * @return This matrix reflected along the main diagonal.
     */
    public Matrix getTransposeMatrix() {
        double[][] transpose = new double[values[0].length][values.length];
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                transpose[j][i] = values[i][j];
            }
        }
        return new Matrix(transpose);
    }
     
    /**
     * Get the values within the matrix.
     * @return the values within the matrix.
     */
    public double[][] getValues() {
        return values;
    }
    
    /**
     * Create an n x n identity matrix.
     * @param n The dimensions of the identity matrix.
     * @return A matrix that has the values of an identity matrix.
     */
    public static Matrix getIdentityMatrix(int n) {
        double[][] values = new double[n][n];
        for(int i = 0; i < n; i++) {
            values[i][i] = 1;
        }
        return new Matrix(values);
    }
    
    /**
     * Check whether the dimensions of the two matrices are the same.
     * @param m1 The first matrix in the comparison.
     * @param m2 The second matrix in the comparison.
     */
    public static void checkDimensions(Matrix m1, Matrix m2) {
        if(m1.n != m2.n || m1.m != m2.m) {
            throw new IllegalArgumentException("The matrix do not have the same dimensions!");
        }
    }
    
    /**
     * Check whether the dimensions of the matrix and vector are the same.
     * @param m1 The matrix in the comparison.
     * @param v1 The vector in the comparison.
     */
    public static void checkDimensions(Matrix m1, Vector v1) {
        if(m1.n != v1.size()) {
            throw new IllegalArgumentException("The matrix and the vector do not share the same n!");
        }
    }
}
