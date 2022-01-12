import { parse } from "@babel/parser";
import generate from "@babel/generator";

import fs from "fs";

const code = fs.readFileSync("codes/code1.js", "utf-8");
let ast = parse(code);

const {code:output} = generate(ast,{
retainLines:true,
});
console.log(output)