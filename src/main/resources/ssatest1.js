// a test file for the parser

var i = 1 + 1;
/*! function int(int, int, int, int, int, int) */
function F(a, b, c, d, e) {
    // test operator precedence
    return a + b * c / d + e;
}

// warn on keywords as identifier
/*! int */ var x = 0, y, z;

x += y = z = 14;

function print(val) {
    console.log(val);
}

if (y === 1 - 1 - 1) {
    print(z);
}

if (x < y) {
    print(x);
} else if (x == y)
    print(y)
else
    print(z);

a = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,13];

for (/*!int */ var i = 0; i < a.length; ++i) {
    print(a[i] * 2);
}