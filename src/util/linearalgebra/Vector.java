package util.linearalgebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Melroy
 */
public class Vector {
    //The values that are contained within the vector, which is a one-dimensional array.
    private final double[] values;
    
    /**
     * Create a vector from the given list of values.
     * 
     * @param values The values that should be within the vector.
     */
    public Vector(double... values) {
        this.values = values;
    }
    
    /**
     * Create a vector in which the given value occurs n times.
     * @param value The value that should be within the vector.
     * @param n The amount of times the above value should occur.
     */
    public Vector(double value, int n) {
        values = new double[n];
        for(int i = 0; i < n; i++) {
            values[i] = value;
        }
    }

    /**
     * Get the array of values contained within this vector.
     * @return The values within the vector, in the form of an array list.
     */
    public double[] getValues() {
        return values;
    }
    
    /**
     * Add the values of the given vector to this vector.
     * @param v1 The vector to add.
     * @return A new vector that is the addition of this vector and the other vector.
     */
    public Vector add(Vector v1) {
        checkDimensions(this, v1);
        double[] values = new double[this.size()];

        for(int i = 0; i < this.size(); i++) {
            values[i] = this.values[i] + v1.values[i];
        }

        return new Vector(values);
    } 
    
    /**
     * Subtract the values of the given vector to this vector.
     * @param v1 The vector to subtract.
     * @return A new vector that is the subtraction of this vector by the other vector.
     */
    public Vector subtract(Vector v1) {
        checkDimensions(this, v1);
        double[] values = new double[this.size()];

        for(int i = 0; i < this.size(); i++) {
            values[i] = this.values[i] - v1.values[i];
        }

        return new Vector(values);
    } 
    
    /**
     * Scale the vector by the given scalar.
     * @param scalar The value to multiply the vector by.
     * @return A new vector that is the scaling of the original vector.
     */
    public Vector scale(double scalar) {
        double[] values = new double[this.size()];

        for(int i = 0; i < this.size(); i++) {
            values[i] = this.values[i] * scalar;
        }

        return new Vector(values);
    } 
    
    /**
     * Get the size of this vector.
     * @return The length of the internal array.
     */
    public int size() {
        return this.values.length;
    }
    
    /**
     * Get the dot product of the two vectors.
     * 
     * @param v1 The vector to multiply with.
     * @return The dot product of the two vectors.
     */
    public double dot(Vector v1) {
        checkDimensions(this, v1);
        double result = 0;
        for(int i = 0; i < this.size(); i++) {
            result += this.values[i] * v1.values[i];
        }
        return result;
    }
    
    /**
     * Get the cross product of the two vectors.
     * 
     * @param v1 The vector to multiply with.
     * @return The cross product of the two vectors.
     */
    public Matrix cross(Vector v1) {
        double[][] matrix = new double[this.size()][v1.size()];
        for(int i = 0; i < this.size(); i++) {
            for(int j = 0; j < v1.size(); j++) {
                matrix[i][j] = this.values[i] * v1.values[j];
            }
        }
        return new Matrix(matrix);
    }
    
    /**
     * Check whether the dimensions of the two vectors are the same.
     * @param v1 The first vector in the comparison.
     * @param v2 The second vector in the comparison.
     */
    public static void checkDimensions(Vector v1, Vector v2) {
        if(v1.size() != v2.size()) {
            throw new IllegalArgumentException("The vectors do not have an equal length!");
        }
    }

    /**
     * Get the value at the i-th position.
     *
     * @param i The index to get the value at.
     * @return The value at the i-th position.
     */
    public double getValue(int i) {
        return values[i];
    }

    @Override
    public String toString() {
        return "Vector{" +
                "values=" + Arrays.toString(values) +
                '}';
    }
}
