import java.util.List;
/**
 * This class is only used to contain information about one basic block.
 * 
 * @author Sébastien Leray
 * @version 1.0
 * @since 2020-10-04
 */
class FunctionContainer {
    private String name;
    private List<BlockContainer> blocks;

    /**
     * Constructor of FunctionContainer.
     * @param name The name of the function.
     * @param blocks The list containing all basic blocks of the function.
     */
    public FunctionContainer(String name, List<BlockContainer> blocks) {
        this.name = name;
        this.blocks = blocks;
    }

    /**
     * This method allow to get the structure of the function at JSON format.
     * @return The string containing the structure of the function at JSON format.
     */
    public String toJson() {

        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append("{\"name\":\"" + this.name + "\",\"blocks\": [");

        for (BlockContainer b: this.blocks) {
            strBuilder.append(b.toJson());
            if (this.blocks.get(this.blocks.size()-1) != b)
                strBuilder.append(",");
        }

        strBuilder.append("]}");
        
        return strBuilder.toString();
    }
}
