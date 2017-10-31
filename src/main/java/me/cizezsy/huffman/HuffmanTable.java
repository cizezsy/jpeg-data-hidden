package me.cizezsy.huffman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HuffmanTable {

    public final static int DC = 0;
    public final static int AC = 1;

    private int id;
    private int type;
    private int[] eachNodeNum;
    private int[] weights;
    private List<TreeNode> treeNodes;

    public HuffmanTable(int id, int type, int[] eachNodeNum, int[] weights) {
        this.id = id;
        this.type = type;
        this.eachNodeNum = Arrays.copyOf(eachNodeNum, eachNodeNum.length);
        this.weights = Arrays.copyOf(weights, weights.length);
        initTree();
    }

    private void initTree() {
        int[] nodeNum = Arrays.copyOf(eachNodeNum, eachNodeNum.length);
        treeNodes = new ArrayList<>();
        TreeNode currentNode = null;
        int currentNodeNumIndex = 0;
        int currentWeightIndex = 0;
        while (currentWeightIndex < weights.length) {
            if (nodeNum[currentNodeNumIndex] == 0) {
                currentNodeNumIndex++;
            } else {
                nodeNum[currentNodeNumIndex]--;
                //TreeNode treeNode = new TreeNode();
                int weight = weights[currentWeightIndex++];
                int length = currentNodeNumIndex + 1;
                int bitCode;
                if (currentNode == null) {
                    bitCode = 0;
                } else {
                    if (currentNode.length == currentNodeNumIndex + 1) {
                        bitCode = currentNode.bitCode + 1;
                    } else {
                        int shift = currentNodeNumIndex + 1 - currentNode.length;
                        bitCode = (currentNode.bitCode + 1) << shift;
                    }
                }
                TreeNode treeNode = new TreeNode(length, bitCode, weight);
                treeNodes.add(treeNode);
                currentNode = treeNode;
            }
        }
    }

    public Optional<TreeNode> findTreeNode(int bitCode, int length) {
        for (TreeNode node : treeNodes) {
            if (node.bitCode == bitCode && node.length == length)
                return Optional.of(node);
        }
        return Optional.empty();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<TreeNode> getTreeNodes() {
        return treeNodes;
    }

    public static class TreeNode {
        int length;
        int bitCode;
        int weight;
        
        private TreeNode(int length, int bitCode, int weight) {
            this.length = length;
            this.bitCode = bitCode;
            this.weight = weight;
        }

        public int getLength() {
            return length;
        }

        public int getBitCode() {
            return bitCode;
        }

        public int getWeight() {
            return weight;
        }

    }
}