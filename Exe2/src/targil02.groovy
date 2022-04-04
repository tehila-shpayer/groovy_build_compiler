/*
Tehila Shpayer 325236594
Sarah Malka Hamou 325266401
Group 150060.21.5782.41 (Nurit)
*/

class Exe2 {
    static int lable_id_cmp = 0
    static void main(String[] args) {
        String path = args[0]
        String last_folder = path.substring(path.lastIndexOf("\\"))
        File assembly_file = new File(path + last_folder + ".asm")
        assembly_file.write("") //if the file already exists, clear file.
        File folder = new File(path)
        folder.eachFile {
            if (it.isFile() && it.getName().endsWith(".vm")) {
                def lines = it.readLines()
                for (line in lines) {
                    List commands = line.split()
                    setCommend(commands, it, assembly_file)
                }
            }
        }
    }
    static String getSegmantAsmName(String seg) {
        String segment
        switch (seg) {
            case 'local':
                segment = "LCL"
                break
            case 'argument':
                segment = "ARG"
                break
            case 'this':
                segment = "THIS"
                break
            case 'that':
                segment = "THAT"
                break
            default:
                segment = "SP"
        }
        return segment
    }
    static String getPointerAsmName(int offset) {
        if(offset)
            return "THAT"
        return "THIS"
    }
    static String getWithFileName(String name, File it) {
        return it.getName() + "." + name
    }
    static void setLabel(String name, File asm_file, File it) {

         asm_file.append("("+ getWithFileName(name, it)+ ")\n")
    }
    static void setGoto(String label, File asm_file, File it) {
        asm_file.append("@" + getWithFileName(label, it) + "\n"
                        + "0;JMP\n")
    }
    static void setIfGoto(String label, File asm_file, File it) {
        asm_file.append("@SP\n" +
                        "A=M-1\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "M=M-1\n" +
                        "@" + getWithFileName(label, it) + "\n" +
                        "D;JNE\n")
    }
    static void setSimpleAritmCommend(String cmd, File asm_file) {
        String op
        switch (cmd) {
            case 'add':
                op = "M=D+M\n"
                break
            case 'sub':
                op = "M=M-D\n"
                break
            case 'and':
                op = "M=M&D\n"
                break
            case 'or':
                op = "M=M|D\n"
                break
            default:
                op = "M=D+M\n"
        }
        asm_file.append("@SP\n" +
                "A=M-1\n" +
                "D=M\n" +
                "A=A-1\n" +
                op +
                "@SP\n" +
                "M=M-1\n")
    }
    static void setNotCommend(File asm_file) {
        asm_file.append("@SP\n" +
                "A=M-1\n" +
                "D=M\n" +
                "M=!D\n")
    }
    static void setCmpCommends(String cmd, File asm_file) {
        String asm_cmp
        switch (cmd) {
            case 'eq':
                asm_cmp = "D;JEQ\n"
                break
            case 'gt':
                asm_cmp = "D;JGT\n"
                break
            case 'lt':
                asm_cmp = "D;JLT\n"
                break
        }
        asm_file.append("@SP\n" +
                "A=M-1\n" +
                "D=M\n" +
                "A=A-1\n" +
                "D=M-D\n" +
                "@IF_TRUE" + lable_id_cmp + "\n" +
                asm_cmp +
                "D=0\n" +
                "@IF_FALSE" + lable_id_cmp + "\n" +
                "0;JMP\n" +
                "(IF_TRUE" + lable_id_cmp + ")\n" +
                "D=-1\n" +
                "(IF_FALSE" + lable_id_cmp + ")\n" +
                "@SP\n" +
                "A=M-1\n" +
                "A=A-1\n" +
                "M=D\n" +
                "@SP\n" +
                "M=M-1\n")
    }
    static void setNegCommend(File asm_file) {
        asm_file.append("@SP\n" +
                "A=M-1\n" +
                "M=-M\n")
    }
    static void setPushCommend(String segment, int offset, File asm_file, File it) {
        switch (segment) {
            case 'local':
            case 'argument':
            case 'this':
            case 'that':
                asm_file.append("@" + offset + "\n" +
                        "D=A\n" +
                        "@" + getSegmantAsmName(segment) + "\n" +
                        "A=M+D\n" +
                        "D=M\n")
                break
            case 'temp':
                int index = offset + 5
                asm_file.append("@" + index + "\n" +
                        "D=M\n")
                break
            case 'static':
                String file_name = it.name.substring(0, it.name.length() - 3)
                asm_file.append("@" + file_name + "." + offset + "\n" +
                        "D=M\n")
                break
            case 'constant':
                asm_file.append("@" + offset + "\n" +
                        "D=A\n")
                break
            case 'pointer':
                asm_file.append("@" + getPointerAsmName(offset) + "\n" +
                        "D=M\n")
                break
        }
        asm_file.append("@SP\n" +
                "A=M\n" +
                "M=D\n" +
                "@SP\n" +
                "M=M+1\n")
    }
    static void setPopCommend(String segment, int offset, File asm_file, File it) {
        switch (segment) {
            case 'local':
            case 'argument':
            case 'this':
            case 'that':
                asm_file.append("@" + offset + "\n" +
                        "D=A\n" +
                        "@" + getSegmantAsmName(segment) + "\n" +
                        "A=M+D\n" +
                        "D=A\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "A=M\n" +
                        "D=A+D\n" +
                        "A=D-A\n" +
                        "M=D-A\n")
                break
            case 'temp':
                int index = offset + 5
                asm_file.append("@" + index + "\n" +
                        "D=A\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "A=M\n" +
                        "D=A+D\n" +
                        "A=D-A\n" +
                        "M=D-A\n")
                break
            case 'static':
                String file_name = it.name.substring(0, it.name.length() - 3)
                asm_file.append("@SP\n" +
                        "A=M-1\n" +
                        "D=M\n" +
                        "@" + file_name + "." + offset + "\n" +
                        "M=D\n")
                break
            case 'pointer':
                asm_file.append("@SP\n" +
                        "A=M-1\n" +
                        "D=M\n" +
                        "@" + getPointerAsmName(offset) + '\n' +
                        "M=D\n")
                break
        }
        asm_file.append("@SP\n" +
                "M=M-1\n")
    }
    static void setCommend(List commands, File it, File asm_file) {
        switch (commands[0]) {
            case 'add':
            case 'sub':
            case 'and':
            case 'or':
                setSimpleAritmCommend(commands[0], asm_file)
                break
            case 'not':
                setNotCommend(asm_file)
                break
            case 'eq':
            case 'gt':
            case 'lt':
                setCmpCommends(commands[0], asm_file)
                lable_id_cmp++
                break
            case 'neg':
                setNegCommend(asm_file)
                break
            case 'push':
                setPushCommend(commands[1], commands[2] as Integer, asm_file, it)
                break
            case 'pop':
                setPopCommend(commands[1], commands[2] as Integer, asm_file, it)
                break
            case 'label':
                setLabel(commands[1], asm_file, it)
                break
            case 'goto':
                setGoto(commands[1], asm_file, it)
                break
            case 'if-goto':
                setIfGoto(commands[1], asm_file, it)


        }
    }
}




