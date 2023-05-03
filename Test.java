class Test {
    public static void main(String[] args) {
        {
            System.out.println(new DistanceCalculator().calc(0, 70, 2));
            System.out.println(new DistanceCalculator().calc(10, 10, 10));
            System.out.println(new DistanceCalculator().calc(0, 0, 10000));
            System.out.println(new Ackermann().Ack(3, 5));
            System.out.println(new SelectionSort().DoSort());
            System.out.println(new Stack().demo());
            System.out.println(new C().g());
        }
    }
}

class DistanceCalculator {
    public int calc(int a, int v, int t) {
        return v * t + this.Div(a) * t * t;
    }

    // Borrowed from BinarySearch.java
    public int Div(int num){
        int count01 ;
        int count02 ;
        int aux03 ;
    
        count01 = 0 ;
        count02 = 0 ;
        aux03 = num - 1 ;
        while (count02 < aux03) {
            count01 = count01 + 1 ;
            count02 = count02 + 2 ;
        }
        return count01 ;	
    }
}

class Ackermann {
    public int Ack(int m, int n) {
        int a;

        if (m < 1)
            a = n + 1;
        else if (n < 1)
            a = this.Ack(m - 1, 1);
        else
            a = this.Ack(m - 1, this.Ack(m, n - 1));

        return a;
    }
}

class SelectionSort {
    public int DoSort() {
        int i;
        int[] arr;
        arr = new int[10];
        arr[0] = 10;
        arr[1] = 3;
        arr[2] = 1;
        arr[3] = 2;
        arr[4] = 4;
        arr[5] = 6;
        arr[6] = 9;
        arr[7] = 8;
        arr[8] = 7;
        arr[9] = 5;

        i = this.Sort(arr);
        i = 0;
        while (i < arr.length) {
            System.out.println(arr[i]);
            i = i + 1;
        }

        return 2147483647;
    }

    public int Sort(int[] arr) {
        int i;
        int j;
        int t;
        int minimum;

        if (0 < arr.length) {
            i = 0;
            while (i < arr.length) {
                minimum = i;

                j = i + 1;
                while (j < arr.length) {
                    if (arr[j] < arr[minimum])
                        minimum = j;
                    else {}

                    j = j + 1;
                }

                t = arr[i];
                arr[i] = arr[minimum];
                arr[minimum] = t;
                i = i + 1;
            }
        } else {}

        return 0;
    }
}

class Stack {
    int capacity;
    int size;
    int[] arr;

    public int demo() {
        int i;
        int t;
        Stack stack;

        stack = new Stack();

        // Push all even numbers in [0-20]
        i = 0;
        while (i < 21) {
            t = stack.push(i);
            i = i + 2;
        }

        // Pop all numbers greater than 10
        while (10 < stack.peek()) {
            t = stack.pop();
        }

        // Push all even numbers in [10-20]
        i = 11;
        while (i < 21) {
            t = stack.push(i);
            i = i + 2;
        }

        // Print all numbers on the stack
        while (0 < stack.size()) {
            System.out.println(stack.pop());
        }

        return stack.pop();
    }

    public int push(int x) {
        int i;
        int[] replace;

        if (size < capacity) {}
        else {
            if (capacity < 1) capacity = 2;
            else capacity = 2 * capacity;

            replace = new int[capacity];

            i = 0;
            while (i < size) {
                replace[i] = arr[i];
                i = i + 1;
            }

            arr = replace;
        }

        arr[size] = x;
        size = size + 1;
        return size;
    }

    public int pop() {
        int popval;

        if (size < 1) {
            popval = 2147483647;
        }
        else {
            size = size - 1;
            popval = arr[size];
        }

        return popval;
    }

    public int peek() {
        return arr[size - 1];
    }

    public int size() {
        return size;
    }
}

class A {
    int x;
}

class B extends A {
    int y;

    public int f() {
        x = 2;
        y = 2;
        return x * y;
    }
}

class C extends B {
    int z;

    public int g() {
        B b;

        x = 2;
        y = 2;
        z = 2;
        b = new B();

        return x * y * z * b.f() * new C().f();
    }
}
