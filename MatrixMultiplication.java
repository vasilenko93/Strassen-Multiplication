// Student name   : Aleksandr Vasilenko
// Student ID     : 218673131
// Year & Semester: Spring 2016
// Class & Section: CSC130-01



import java.util.Random;

public class MatrixMultiplication {

  public static void main( String[] args ) {
    int n;
    if( args.length >= 1) {
      n = Integer.parseInt(args[0]);
    } else {
      n = 4;
      System.out.println( "Matrix size not specified, using default of 4 x 4." );
      System.out.println( "To use a diffrent size run application again with a command line argument." );
      System.out.println( "Type: 'java MatrixMultiplication N' for a N x N matrix\n\n" );
    }

    Matrix a = new Matrix( n, true );
    Matrix b = new Matrix( n, true );

    System.out.println( "Random Matrix A = " );
    a.printMatrix();
    System.out.println( "Random Matrix B = " );
    b.printMatrix();

    Matrix c = Strassen( a, b );
    System.out.println( "C = Strassen(A,B)" );
    System.out.println( "Matrix C = " );
    c.printMatrix();
  }

  public static Matrix Strassen( Matrix a, Matrix b ) {
    int n = a.getN();

    Matrix output = new Matrix( n, false );


    if( n == 1 ) {
      int product = a.getValue( 0, 0 ) * b.getValue( 0, 0 );
      output.setValue( 0, 0, product );
      return output;
    }

    a = Matrix.addPadding( a );
    b = Matrix.addPadding( b );
    output = Matrix.addPadding( output );

    // Compute A11, A12, ... , B21, B22
    Matrix a11 = Matrix.splitMatrix( a, 0 );
    Matrix a12 = Matrix.splitMatrix( a, 1 );
    Matrix a21 = Matrix.splitMatrix( a, 2 );
    Matrix a22 = Matrix.splitMatrix( a, 3 );
    Matrix b11 = Matrix.splitMatrix( b, 0 );
    Matrix b12 = Matrix.splitMatrix( b, 1 );
    Matrix b21 = Matrix.splitMatrix( b, 2 );
    Matrix b22 = Matrix.splitMatrix( b, 3 );

    Matrix p1 = Strassen( a11, Matrix.subtractMatrixies(b12, b22) );
    Matrix p2 = Strassen( Matrix.addMatrixies(a11, a12), b22 );
    Matrix p3 = Strassen( Matrix.addMatrixies(a21, a22), b11 );
    Matrix p4 = Strassen( a22, Matrix.subtractMatrixies(b21, b11) );
    Matrix p5 = Strassen( Matrix.addMatrixies(a11, a22), Matrix.addMatrixies(b11, b22) );
    Matrix p6 = Strassen( Matrix.subtractMatrixies(a12, a22), Matrix.addMatrixies(b21, b22) );
    Matrix p7 = Strassen( Matrix.subtractMatrixies(a11, a21), Matrix.addMatrixies(b11, b12) );

    // Asign C11, C12, C21, C22
    output.mergeMatrix( Matrix.addMatrixies(Matrix.subtractMatrixies(Matrix.addMatrixies(p5,p4),p2),p6), 0 );
    output.mergeMatrix( Matrix.addMatrixies(p1, p2), 1 );
    output.mergeMatrix( Matrix.addMatrixies(p3, p4), 2 );
    output.mergeMatrix( Matrix.subtractMatrixies(Matrix.subtractMatrixies(Matrix.addMatrixies(p1,p5),p3),p7), 3 );


    output = Matrix.removePadding( output );
    return output;
  }


}
// End of class






class Matrix {
  private int[][] matrix;

  private int n; //Number of rows and columns
  private int paddingAmmount;

  public Matrix( int n, boolean random ) {
    this.n = n;
    paddingAmmount = 0;

    if( random )
      generateRandomMatrix( );
    else
      generateEmptyMatrix();
  }

  public int getN() {
    return n;
  }

  public int getPadding( ) {
    return paddingAmmount;
  }

  public void setValue( int row, int col, int value ) {
    this.matrix[row][col] = value;
  }

  public int getValue( int row, int col ) {
    return this.matrix[row][col];
  }

  public static Matrix addMatrixies( Matrix a, Matrix b ) {
    int n = a.getN();
    int sumValue;
    Matrix sumMatrix = new Matrix( n, false );
    for( int row = 0; row < n; row++ ) {
      for( int col = 0; col < n; col++ ) {
        sumValue = a.getValue( row, col ) + b.getValue( row, col );
        sumMatrix.setValue(row, col, sumValue );
      }
    }
    return sumMatrix;
  }

  public static Matrix subtractMatrixies( Matrix a, Matrix b ) {
    int n = a.getN();
    int sumValue;
    Matrix sumMatrix = new Matrix( n, false );
    for( int row = 0; row < n; row++ ) {
      for( int col = 0; col < n; col++ ) {
        sumValue = a.getValue( row, col ) - b.getValue( row, col );
        sumMatrix.setValue(row, col, sumValue );
      }
    }
    return sumMatrix;
  }

  // Index is quadrant of matrix
  // +-----+  // returns a submatrix
  // | 0 1 |  // at the specified quadrant
  // | 2 3 |
  // +-----+
  public static Matrix splitMatrix( Matrix matrix, int index ) {
    int m = matrix.getN() / 2;

    Matrix temp = new Matrix( m, false );

    int colShift;
    int rowShift;
    switch( index ) {
      case 0: rowShift = 0; colShift = 0; break;
      case 1: rowShift = 0; colShift = m; break;
      case 2: rowShift = m; colShift = 0; break;
      case 3: rowShift = m; colShift = m; break;
      default: colShift = 0; rowShift = 0; break;
    }

    for( int row = rowShift; row < rowShift + m; row++ ) {
      for( int col = colShift; col < colShift + m; col++ ) {
        temp.setValue( row - rowShift, col - colShift, matrix.getValue(row, col) );
      }
    }
    return temp;
  }

  // Index is quadrant of matrix
  // +-----+  // The smaller submatrix will
  // | 0 1 |  // be merged into the bigger
  // | 2 3 |  // matrix at a specified quadrant
  // +-----+  // Only works if submatrix is exactly 2 times smaller
  public void mergeMatrix( Matrix subMatrix, int index ) {
    int m = this.getN() / 2;
    if( m == 0 ) { m++; }
    int colShift;
    int rowShift;
    switch( index ) {
      case 0: rowShift = 0; colShift = 0;  break;
      case 1: rowShift = 0; colShift = m;  break;
      case 2: rowShift = m; colShift = 0;  break;
      case 3: rowShift = m; colShift = m;  break;
      default: colShift = 0; rowShift = 0; break;
    }

    for( int row = rowShift; row < rowShift + m; row++ ) {
      for( int col = colShift; col < colShift + m; col++ ) {
        this.matrix[row][col] = subMatrix.matrix[row - rowShift][col - colShift];
      }
    }
  }

  // Copies N - ammount rows and cols
  public void copyMatrix( Matrix oldMatrix, int ammount) {
    int n = oldMatrix.getN();
    for( int row = 0; row < n - ammount; row++ ) {
      for( int col = 0; col < n - ammount; col++ ) {
        this.matrix[row][col] = oldMatrix.matrix[row][col];
      }
    }
  }

  // Used to add extra zeros at corners of matrix
  // To make Strassen work with any NxN matrix
  public static Matrix addPadding( Matrix oldMatrix ) {
    // Check if a number is a power of 2
    // Nice piece of code goes to:
    // http://stackoverflow.com/a/19383296
    int n = oldMatrix.getN();
    if( (n & (n - 1)) == 0 ) {
      return oldMatrix;
    }

    int pow2 = nextPow2( n );
    Matrix newMatrix = new Matrix( pow2, false );
    newMatrix.copyMatrix( oldMatrix, 0 );
    newMatrix.changePaddingAmmount( pow2 - n );
    return newMatrix;
  }

  public static Matrix removePadding( Matrix oldMatrix ) {
    int padding = oldMatrix.getPadding();
    if( padding == 0 ) {
      return oldMatrix;
    }

    Matrix newMatrix = new Matrix( oldMatrix.getN() - padding, false );
    newMatrix.copyMatrix( oldMatrix, padding );
    return newMatrix;
  }

  public void changePaddingAmmount( int paddingAmmount ) {
    this.paddingAmmount = paddingAmmount;
  }

  private void generateRandomMatrix( ) {
    Random rand = new Random();
    int max = 25, min = -25;

    this.matrix = new int[this.n][this.n];

    for( int row = 0; row < this.n; row++ ) {
      for( int col = 0; col < this.n; col++ ) {
        this.matrix[row][col] = rand.nextInt((max - min) + 1) + min;
      }
    }
  }

  private void generateEmptyMatrix( ) {
    this.matrix = new int[this.n][this.n];

    for( int row = 0; row < this.n; row++ ) {
      for( int col = 0; col < this.n; col++ ) {
        this.matrix[row][col] = 0;
      }
    }
  }

  // Used to get the next power of 2.
  // 5 returns 8, 7 returns 8, and 9 returns 16
  public static int nextPow2( int n ) {
    int pow2 = 1;
    while( n > pow2 ) {
      pow2 = pow2 << 1;
    }
    return pow2;
  }

  public void printMatrix( ) {
    printHorizonalLine(this.n * 6 + 2);
    for( int row = 0; row < this.n; row++ ) {
      System.out.print( "|" );
      for( int col = 0; col < this.n; col++ ) {
        System.out.printf("%5d ", this.matrix[row][col]);
      }
      System.out.println( "|" );
    }
    printHorizonalLine(this.n * 6 + 2);
  }

  private void printHorizonalLine( int length ) {
    System.out.print( "+---" );
    for( int i = 0; i < length - 2 - 6; i++ ) {
      System.out.print( " " );
    }
    System.out.println( "---+" );
  }

}
