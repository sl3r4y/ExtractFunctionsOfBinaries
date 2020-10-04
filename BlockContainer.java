/**
 * This class is only used to contain information about one function.
 * 
 * @author Sébastien Leray
 * @version 1.0
 * @since 2020-10-04
 */
class BlockContainer {
    private String label;
    private List<BlockContainer> sourceBlocks;
    private List<BlockContainer> destinationBlocks;
    private List<String> normalizedInstructions;

    /**
     * Constructor of BlockContainer.
     * @param label This is the name of the block.
     * @param sourceBlocks This is the list containing all blocks which jump to this block.
     * @param destinationBlocks This is the list containing all blocks towards this block jumps.
     * @param normalizedInstructions This is contains all instructions which have been normalized.
     */
    public BlockContainer(String label, List<BlockContainer> sourceBlocks, List<BlockContainer> destinationBlocks, List<String> normalizedInstructions) {
        this.label = label;
        this.sourceBlocks = sourceBlocks;
        this.destinationBlocks = destinationBlocks;
        this.normalizedInstructions = normalizedInstructions;
        
    }

    /**
     * This method allows to return the name of this block.
     * @return This return the label of this block.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * This method allows to change the list of source blocks.
     * @param sourceBlocks The new list of source blocks.
     */
    public void setSourceBlocks(List<BlockContainer> sourceBlocks) {
        this.sourceBlocks = sourceBlocks;
    }

    /**
     * This method allows to add a block like a source block.
     * @param sourceBlock The block to add.
     * @return True if the block is not contains in the list.
     *         False otherwise.
     */
    public boolean addInSourceBlocks(BlockContainer sourceBlock) {
        if (this.sourceBlocks.add(sourceBlock))
            return true;
        else
            return false;
    }
    
    /**
     * This method allows to get the source block list.
     * @return The source block list.
     */
    public List<BlockContainer> getSourceBlocks() {
        return this.sourceBlocks;
    }

    /**
     * This method allows to change the list of destination blocks.
     * @param destinationBlocks The new list of destination blocks.
     */
    public void setDestinationBlocks(List<BlockContainer> destinationBlocks) {
        this.destinationBlocks = destinationBlocks;
    }

    /**
     * This method allows to add a block like a destination block.
     * @param destinationBlock The block to add.
     * @return True if the block is not contains in the list.
     *         False otherwise.
     */
    public boolean addInDestinationBlocks(BlockContainer destinationBlock) {
        if (this.destinationBlocks.add(destinationBlock))
            return true;
        else
            return false;
    }

    /**
     * This method allows to get the destination block list.
     * @return The destination block list.
     */
    public List<BlockContainer> getDestinationBlocks() {
        return this.destinationBlocks;
    }

    /**
     * This method allow to get the structure of the basic block at JSON format.
     * @return The string containing the structure of the basic block at JSON format.
     */
    public String toJson() {
        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append("{\"label\":\"" + this.label + "\",\"normalizedInst\":[");
        
        for (String inst: this.normalizedInstructions) {
            strBuilder.append("\"" + inst +"\"");
            if (this.normalizedInstructions.get(this.normalizedInstructions.size()-1) != inst)
                strBuilder.append(",");
        }

        strBuilder.append("],\"sourceBlock\": [");
        
        for (BlockContainer b: this.sourceBlocks) {
            strBuilder.append("\"" + b.getLabel() + "\"");
            if (this.sourceBlocks.get(this.sourceBlocks.size()-1) != b)
                strBuilder.append(",");
        }

        strBuilder.append("],\"destinationBlock\": [");

        for (BlockContainer b: this.destinationBlocks) {
            strBuilder.append("\"" + b.getLabel() + "\"");
            if (this.destinationBlocks.get(this.destinationBlocks.size()-1) != b)
                strBuilder.append(",");
        }

        strBuilder.append("]}");
                  
        return strBuilder.toString();
    }

}