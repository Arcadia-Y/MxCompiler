#define bool _Bool
#define sys64

#ifdef sys64
    typedef unsigned long size_t;
#else
    typedef unsigned int size_t;
#endif

int printf(const char *pattern, ...);
int sprintf(char *dest, const char *pattern, ...);
int scanf(const char *pattern, ...);
int sscanf(const char *src, const char *pattern, ...);
size_t strlen(const char *str);
int strcmp(const char *s1, const char *s2);
void* memcpy(void *dest, const void *src, size_t n);
void* malloc(size_t n);

void print(char* s) {
    printf("%s", s);
}

void println(char* s) {
    printf("%s\n", s);
}

void printInt(int x) {
    printf("%d", x);
}

void printlnInt(int x) {
    printf("%d\n", x);
}

char* getString() {
    char* buf = malloc(2048);
    scanf("%s", buf);
    return buf;
}

int getInt() {
    int res;
    scanf("%d", &res);
    return res;
}

char* toString(int x) {
    char* buf = malloc(16);
    sprintf(buf, "%d", x);
    return buf;
}

int string_length(char* s) {
    return strlen(s);
}

char* string_substring(char* s, int l, int r) {
    int len = r - l;
    char* buf = malloc(len+1);
    memcpy(buf, s+l, len);
    buf[len] = '\0';
    return buf;
}

int string_parseInt(char* s) {
    int res;
    sscanf(s, "%d", &res);
    return res;
}

int string_ord(char* s, int i) {
    return (int)s[i];
}

char* string_add(char* s1, char* s2) {
    int len1 = strlen(s1), len2 = strlen(s2);
    char* res = malloc(len1 + len2 + 1);
    memcpy(res, s1, len1);
    memcpy(res + len1, s2, len2);
    res[len1+len2] = '\0';
    return res;
}

bool string_eq(char* s1, char* s2) {
    return strcmp(s1, s2) == 0;
}

bool string_neq(char* s1, char* s2) {
    return strcmp(s1, s2) != 0;
}

bool string_gt(char* s1, char* s2) {
    return strcmp(s1, s2) > 0;
}

bool string_gte(char* s1, char* s2) {
    return strcmp(s1, s2) >= 0;
}

bool string_lt(char* s1, char* s2) {
    return strcmp(s1, s2) < 0;
}

bool string_lte(char* s1, char* s2) {
    return strcmp(s1, s2) <= 0;
}

int _array_size(void* arr) {
    return ((int*)arr)[-1];
}

void* new_malloc(int n) {
    return malloc(n);
}

void* new_intArray(int n) {
    int* arr = malloc(4*(n+1));
    arr[0] = n;
    return arr+1;
}

void* new_boolArray(int n) {
    int* arr = malloc(n+4);
    arr[0] = n;
    return arr+1;
}

void* new_ptrArray(int n) {
    int* arr;
    #ifdef sys64
        arr = malloc(8*n + 4);
    #else
        arr = malloc(4*(n+1));
    #endif
    arr[0] = n;
    return arr+1;
}
