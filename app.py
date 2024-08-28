from flask import Flask, request, jsonify
from flask_cors import CORS
import ast
import astor
import time
import sys
import tracemalloc

app = Flask(__name__)
CORS(app)

class ComplexityVisitor(ast.NodeVisitor):
    def __init__(self):
        self.loop_depth = 0
        self.max_loop_depth = 0
        self.has_recursion = False
        self.has_log_operations = False
        self.has_sorting = False

    def visit_For(self, node):
        self.loop_depth += 1
        self.max_loop_depth = max(self.max_loop_depth, self.loop_depth)
        self.generic_visit(node)
        self.loop_depth -= 1

    def visit_While(self, node):
        self.loop_depth += 1
        self.max_loop_depth = max(self.max_loop_depth, self.loop_depth)
        self.generic_visit(node)
        self.loop_depth -= 1

    def visit_FunctionDef(self, node):
        if any(isinstance(stmt, ast.Return) for stmt in node.body):
            for stmt in node.body:
                if isinstance(stmt, ast.Return):
                    if isinstance(stmt.value, ast.Call) and stmt.value.func.id == node.name:
                        self.has_recursion = True
        self.generic_visit(node)

    def visit_Call(self, node):
        if isinstance(node.func, ast.Name):
            if node.func.id in ['log', 'log2', 'log10']:
                self.has_log_operations = True
            elif node.func.id in ['sort', 'sorted']:
                self.has_sorting = True
        self.generic_visit(node)

def analyze_static_complexity(code):
    tree = ast.parse(code)
    visitor = ComplexityVisitor()
    visitor.visit(tree)

    if visitor.has_recursion and visitor.has_log_operations:
        return "O(log n)", "Recursive function with logarithmic operations detected."
    elif visitor.has_recursion:
        return "O(2^n) or O(n!)", "Recursive function detected, potentially exponential or factorial complexity."
    elif visitor.max_loop_depth > 1:
        return f"O(n^{visitor.max_loop_depth})", f"Nested loops with depth {visitor.max_loop_depth} detected."
    elif visitor.max_loop_depth == 1:
        if visitor.has_log_operations:
            return "O(n log n)", "Single loop with logarithmic operations detected."
        elif visitor.has_sorting:
            return "O(n log n)", "Sorting operation detected, assuming efficient sort."
        else:
            return "O(n)", "Single loop detected."
    elif visitor.has_log_operations:
        return "O(log n)", "Logarithmic operations detected."
    else:
        return "O(1)", "No loops or complex operations detected."

def analyze_dynamic_complexity(code):
    def wrapper(n):
        exec(code)

    sizes = [10, 100, 1000, 10000]
    times = []
    memories = []

    for size in sizes:
        # Time complexity
        start_time = time.time()
        wrapper(size)
        end_time = time.time()
        times.append(end_time - start_time)

        # Space complexity
        tracemalloc.start()
        wrapper(size)
        _, peak = tracemalloc.get_traced_memory()
        tracemalloc.stop()
        memories.append(peak)

    # Simple complexity estimation based on growth
    time_ratio = times[-1] / times[0]
    space_ratio = memories[-1] / memories[0]

    if time_ratio > 1000:
        time_complexity = "Exponential or worse"
    elif time_ratio > 100:
        time_complexity = "O(n^2) or worse"
    elif time_ratio > 10:
        time_complexity = "O(n log n) or O(n)"
    else:
        time_complexity = "O(1) or O(log n)"

    if space_ratio > 100:
        space_complexity = "O(n) or worse"
    elif space_ratio > 10:
        space_complexity = "O(log n) or O(sqrt(n))"
    else:
        space_complexity = "O(1)"

    return time_complexity, space_complexity

def analyze_complexity(code):
    try:
        static_time, static_reason = analyze_static_complexity(code)
        dynamic_time, dynamic_space = analyze_dynamic_complexity(code)

        return {
            "staticTimeComplexity": static_time,
            "staticTimeReason": static_reason,
            "dynamicTimeComplexity": dynamic_time,
            "spaceComplexity": dynamic_space,
            "note": "Dynamic analysis is based on runtime behavior and may vary with input."
        }
    except Exception as e:
        return {"error": str(e)}

@app.route('/analyze', methods=['POST'])
def analyze():
    code = request.json['code']
    return jsonify(analyze_complexity(code))

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3001)