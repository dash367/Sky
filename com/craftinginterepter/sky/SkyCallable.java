package com.craftinginterepter.sky;

import java.util.List;

public interface SkyCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> argument);
}
