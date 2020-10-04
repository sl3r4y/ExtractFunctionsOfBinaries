import ghidra.app.script.GhidraScript;

import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.InstructionIterator;

import ghidra.program.database.code.InstructionDB;

import ghidra.program.model.block.BasicBlockModel;
import ghidra.program.model.block.CodeBlock;
import ghidra.program.model.block.CodeBlockIterator;
import ghidra.program.model.block.CodeBlockReferenceIterator;
import ghidra.program.model.block.CodeBlockReference;

import ghidra.program.model.address.AddressFactory;
import ghidra.program.model.address.Address;

import ghidra.program.model.symbol.FlowType;

import ghidra.util.task.TaskMonitor;

import java.util.List;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * This script will extract basic blocks for each functions in the binary.
 * 
 * @author Sébastien Leray
 * @version 1.0
 * @since 2020-10-04
 */
public class ExtractFunctions extends GhidraScript {

    /**
     * This is an array containing the list of all registers used in x86_64 architecture.
     */
    final static String[] REGS_X86_64 = {"AH", "AL", "AX", "EAX", "RAX", "BH", "BL", "BX", "EBX", "RBX", "CH", "CL", "ECX", "RCX", "DH", "DL", "EDX", "RDX",
                                         "BPL", "BP", "EBP", "RBP", "SPL", "SP", "ESP", "RSP", "SIL", "SI", "ESI", "RSI", "DIL", "DI", "EDI", "RDI", "R8", 
                                         "R8B", "R8D", "R8W", "R9", "R9B", "R9D", "R9W", "R10", "R10B", "R10D", "R10W", "R11", "R11B", "R11D", "R11W", "R12", 
                                         "R12B", "R12D", "R12W", "R13", "R13B", "R13D", "R13W", "R14", "R14B", "R14D", "R14W", "R15", "R15B", "R15D", "R15W",
                                         "XMM0", "YMM0", "ZMM0", "XMM1", "YMM1", "ZMM1", "XMM2", "YMM2", "ZMM2", "XMM3", "YMM3", "ZMM3", "XMM4", "YMM4",
                                         "ZMM4", "XMM5", "YMM5", "ZMM5", "XMM6", "YMM6", "ZMM6", "XMM7", "YMM7", "ZMM7", "XMM8", "YMM8", "ZMM8", "XMM9", 
                                         "YMM9", "ZMM9", "XMM10", "YMM10", "ZMM10", "XMM11", "YMM11", "ZMM11", "XMM12", "YMM12", "ZMM12", "XMM13", "YMM13",
                                         "ZMM13", "XMM14", "YMM14", "ZMM14", "XMM15", "YMM15", "ZMM15", "ZMM16", "ZMM17", "ZMM18", "ZMM19", "ZMM20", "ZMM21",
                                         "ZMM22", "ZMM23", "ZMM24", "ZMM25", "ZMM26", "ZMM27", "ZMM28", "ZMM29", "ZMM30", "ZMM31", "MM0", "ST(0)", "MM1",
                                         "ST(1)", "MM2", "ST(2)", "MM3", "ST(3)", "MM4", "ST(4)", "MM5", "ST(5)", "MM6", "ST(6)", "MM7", "ST(7)", "CR0", "CR1",
                                         "CR2", "CR3", "CR4", "CR5", "CR6", "CR7", "CR8", "CR9", "CR10", "CR11", "CR12", "CR13", "CR14", "CR15", "MXCSR", "DR0",
                                         "DR1", "DR2", "DR3", "DR4", "DR5", "DR6", "DR7", "DR8", "DR9", "DR10", "DR11", "DR12", "DR13", "DR14", "DR15", "CS", "SS",
                                         "DS", "ES", "FS", "GS", "GDTR", "IDTR", "TR", "LDTR", "CW", "FP_IP", "FP_DP", "FP_CS", "SW", "TW", "FP_DS", "FP_OPC",
                                         "FP_DP", "FP_IP", "MSW", "IP", "EIP", "RIP"};

    /**
     * This is an array containing the list of all registers used in ARM architecture.
     */
    final static String[] REGS_ARM = {"R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "FP", "R12", "IP", "R13", "SP", "R14", "LR", "R15", "PC", "CPSR",
                                      "X0", "W0", "X1", "W1", "X2", "W2", "X3", "W3", "X4", "W4", "X5", "W5", "X6", "W6", "X7", "W7", "X8", "W8", "X9", "W9", "X10", "W10",
                                      "X11", "W11", "X12", "W12", "X13", "W13", "X14", "W14", "X15", "W15", "X16", "W16", "X17", "W17", "X18", "W18", "X19", "W19",
                                      "X20", "W20", "X21", "W21", "X22", "W22", "X23", "W23", "X24", "W24", "X25", "W25", "X26", "W26", "X27", "W27", "X28", "W28",
                                      "X29", "W29", "X30", "W30", "S0", "D0", "V0", "S1", "D1", "V1", "S2", "D2", "V2", "S3", "D3", "V3", "S4", "D4", "V4", "S5", "D5",
                                      "V5", "S6", "D6", "V6", "S7", "D7", "V7", "S8", "D8", "V8", "S9", "D9", "V9", "S10", "D10", "V10", "S11", "D11", "V11",
                                      "S12", "D12", "V12", "S13", "D13", "V13", "S14", "D14", "V14", "S15", "D15", "V15", "S16", "D16", "V16", "S17", "D17", "V17",
                                      "S18", "D18", "V18", "S19", "D19", "V19", "S20", "D20", "V20", "S21", "D21", "V21", "S22", "D22", "V22", "S23", "D23", "V23",
                                      "S24", "D24", "V24", "S25", "D25", "V25", "S26", "D26", "V26", "S27", "D27", "V27", "S28", "D28", "V28", "S29", "D29", "V29",
                                      "S30", "D30", "V30", "S31", "D31", "V31"};
    
    @Override
    public void run() throws Exception {

        BasicBlockModel basicBlockModel = new BasicBlockModel(this.currentProgram);
        Listing listing = this.currentProgram.getListing();
        FunctionIterator functionIterator = this.currentProgram.getFunctionManager().getFunctions(true);

        FileWriter fileWriter = null;

        // We open a file in order to write the structure of all functions at JSON format inside.
        try {
            fileWriter = new FileWriter("extrated_functions.json");
            fileWriter.write("[");
            
            // Here we browse each function in order to extract basic blocks.
            for (Function f: functionIterator) {
                
                List<BlockContainer> blocks = new ArrayList<>();
                FunctionContainer functionContainer = new FunctionContainer(f.getName(), blocks);

                CodeBlockIterator codeBlockIt = basicBlockModel.getCodeBlocksContaining(f.getBody(), TaskMonitor.DUMMY);
                
                // We put in the list all basic blocks.
                List<CodeBlock> codeBlocks = new ArrayList<>();
                while (codeBlockIt.hasNext())
                    codeBlocks.add(codeBlockIt.next());
                
                // We create the BlockContainer which will contain the information of basic block.
                // We use them in order to get the structure of basic block at JSON format.
                for (CodeBlock c: codeBlocks) {
                    List<Instruction> instructions = new ArrayList<>();
                    listing.getInstructions(c, true).forEachRemaining(instructions::add);

                    List<String> normalizedInstructions = this.normalizeInstructions(instructions);
                    blocks.add(new BlockContainer(c.getName(), new ArrayList<BlockContainer>(), new ArrayList<BlockContainer>(), normalizedInstructions));
                }
                
                // Now we add source and destination block for all basic block of the function.
                for (BlockContainer blockContainer: blocks) {
                    
                    CodeBlock currentCodeBlock = null;

                    for (CodeBlock c: codeBlocks)
                        if (c.getName().equals(blockContainer.getLabel())) {
                            currentCodeBlock = c;
                            break;
                        }

                    if (currentCodeBlock == null)
                        throw new Exception("Impossible Case !");
                    
                    // Source blocks.
                    CodeBlockReferenceIterator codeBlockRefIt = currentCodeBlock.getSources(TaskMonitor.DUMMY);
            
                    while (codeBlockRefIt.hasNext()) {
                        CodeBlock codeBlockSource = codeBlockRefIt.next().getSourceBlock();

                        for (BlockContainer blockContainerSource: blocks)
                            if (codeBlockSource.getName().equals(blockContainerSource.getLabel())) {
                                blockContainer.addInSourceBlocks(blockContainerSource);
                                break;
                            }
                    }
                    
                    // Destination blocks.
                    codeBlockRefIt = currentCodeBlock.getDestinations(TaskMonitor.DUMMY);
                
                    while (codeBlockRefIt.hasNext()) {
                        CodeBlock codeBlockDestination = codeBlockRefIt.next().getDestinationBlock();

                        for (BlockContainer blockContainerDestination: blocks)
                            if (codeBlockDestination.getName().equals(blockContainerDestination.getLabel())) {
                                blockContainer.addInDestinationBlocks(blockContainerDestination);
                                break;
                            }
                    }

                }
                
                fileWriter.write(functionContainer.toJson());

                if (functionIterator.hasNext())
                    fileWriter.write(",");
        
            }

            fileWriter.write("\n]");

        } catch (Exception e) {
            System.err.println("Error !");
        } finally {
            fileWriter.close();
        }
        
    }

    /**
     * This method allows to normalize the instructions.
     * @param instructions The list containing the instructions.
     * @return The list containing the normalized instructions.
     */
    private List<String> normalizeInstructions(List<Instruction> instructions) {
        List<String> normalizeInstructions = new ArrayList<>();

        for (Instruction inst: instructions) {
            StringBuilder normInstStrBuilder = new StringBuilder();

            String instStr = ((InstructionDB)inst).toString();

            String mnemonic = instStr.split(" ")[0];

            FlowType flowType = inst.getFlowType();

            // CALL
            if (flowType.isCall()) {
                normInstStrBuilder.append(mnemonic +" ADDRESS");
            } 
            // JUMP
            else if (flowType.isJump()) {
                normInstStrBuilder.append(mnemonic + " ADDRESS");
            } 
            // TERMINAL
            else if (flowType.isTerminal()) {
                normInstStrBuilder.append(mnemonic);
            }
            // OPERATION
            else {
                String[] tmp = instStr.split(" ", 2);

                if (tmp.length > 1) {
                    String[] operands = tmp[1].split(",");
                    String normalizedOperands = this.toNormalizeOperands(operands);
                    normInstStrBuilder.append(mnemonic + " " + normalizedOperands);
                } else
                    normInstStrBuilder.append(mnemonic);
                
            }
            normalizeInstructions.add(normInstStrBuilder.toString());
        }
        
        return normalizeInstructions;
    }

    /**
     * This method allows to know if val is a register.
     * @param val The string which is may be a register.
     * @return True if val is a register.
     *         False otherwise.
     */
    private boolean isRegister(String val) {

        for (int i = 0; i < ExtractFunctions.REGS_X86_64.length; i++)
            if (ExtractFunctions.REGS_X86_64[i].equals(val.replace(" ", "").toUpperCase()))
                return true;

        for (int i = 0; i < ExtractFunctions.REGS_ARM.length; i++)
            if (ExtractFunctions.REGS_ARM[i].equals(val.replace(" ", "").toUpperCase()))
                return true;
        
        return false;
    }

    /**
     * This method allows to know if operand is an address.
     * @param operand The string which is may be an address.
     * @return True if operand is an address.
     *         False otherwise.
     */
    private boolean isAddress(String operand) {
        AddressFactory addrFactory = this.currentProgram.getAddressFactory();

        Address addr = addrFactory.getAddress(operand);

        if (addr == null)
            return false;
        
        Function function = this.currentProgram.getFunctionManager().getFunctionContaining(addr);

        if (function == null)
            return false;
        
        return true;
    }

    /**
     * This method allows to normalize operands.
     * @param operands The list of operands.
     * @return The list of normalized operands.
     */
    private String toNormalizeOperands(String[] operands) {
        StringBuilder operandsNormalized = new StringBuilder();

        for (int i = 0; i < operands.length; i++) {
            
            // MEMORY ACCESS
            if (operands[i].matches(".*\\[.*\\].*"))
                operandsNormalized.append("MEM");

            // VAL : VAL
            else if (operands[i].contains(":")) {
                String[] vals = operands[i].split(":");
                String firstVal;
                String secondVal;

                if (vals[0].matches(".*\\[.*\\].*"))
                    firstVal = "MEM";
                else if (this.isRegister(vals[0]))
                    firstVal = "REG";
                else
                    firstVal = vals[0];
                
                if (vals[1].matches(".*\\[.*\\].*"))
                    secondVal = "MEM";
                else if (this.isRegister(vals[1]))
                    secondVal = "REG";
                else
                    secondVal = vals[1];
                
                operandsNormalized.append(firstVal + ":" + secondVal);
                    
            }
            
            // REGISTER
            else if (this.isRegister(operands[i]))
                operandsNormalized.append("REG");

            // CONSTANT
            else
                operandsNormalized.append(operands[i]);
            
            if (i < (operands.length - 1))
                operandsNormalized.append(", ");

        }
        return operandsNormalized.toString();
    }

}