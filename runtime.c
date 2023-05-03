#include <stdio.h>
#include <stdlib.h>

int __access_array__(int *ptr, int word) {
    return ptr[word + 1];
}

int __access_object__(int *ptr, int word) {
    return ptr[word];
}

int *__alloc_array__(int words) {
    int *p = (int *)calloc(words + 1, sizeof(int));
    *p = words;
    return p;
}

int *__alloc_object__(int words) {
    return (int *)calloc(words, sizeof(int));
}

int __assign_array__(int *ptr, int word, int val) {
    return ptr[word + 1] = val;
}

int __assign_object__(int *ptr, int word, int val) {
    return ptr[word] = val;
}

int __array_length__(int *p) {
    return *p;
}

int __less_than__(int x, int y) {
    return x < y;
}

int __multiply__(int x, int y) {
    return x * y;
}

int __print_int__(int x) {
    return printf("%d\n", x);
}
