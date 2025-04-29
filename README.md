<h1>Sky Programming Language</h1>
Sky is a lightweight, interpreted programming language implemented in Java. This project is inspired by the "Crafting Interpreters" book and serves as an educational tool for understanding language design and interpreter implementation.​

<h2>Features</h2>
<ul>
  <li>Custom grammar defined in jsky-grammar.txt (close to Python)</li>

  <li>Sample script test.sky shows language capability to perform a recursion</li>

  <li>Java implementation under com/craftinginterepter/sky</li>​
</ul>

<h2>Getting Started</h2>
<h3>Prequisite</h3>
Java Development Kit (JDK) 8 or higher​

<h3>Compile & Run</h3>
To compile the project, navigate to the root directory and run:​

```bash
javac com/craftinginterepter/sky/*.java
```

After compilation, you can run the interpreter with:​
```bash
java com.craftinginterepter.sky.Main test.sky
```
<h2>Code Sample</h2>
<h3>Sample (Fibonacci Series):</h3>

```python
fun fib(n) {
    if (n <= 1) 
        return n;
    return fib(n - 2) + fib(n - 1);
}

for (var i = 0; i < 20; i++) {
    print(fib(i));
}
```
<h3>Output:</h3>

```
0  
1  
1  
2  
3  
5  
8  
13  
21  
34  
55  
89  
144  
233  
377  
610  
987  
1597  
2584  
4181  
```
