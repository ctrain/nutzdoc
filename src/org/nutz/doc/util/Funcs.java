package org.nutz.doc.util;

import org.nutz.doc.meta.ZIndex;
import org.nutz.lang.util.LinkedIntArray;
import org.nutz.lang.util.Node;

public abstract class Funcs {

	public static String evalAnchorName(String text) {
		StringBuilder sb = new StringBuilder();
		char[] cs = text.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			switch (cs[i]) {
			case ' ':
			case '\t':
				if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '_') {
					sb.append('_');
				}
				break;
			case '\r':
			case '\n':
			case '"':
			case '.':
			case '\'':
				break;
			default:
				sb.append(cs[i]);
			}
		}
		return sb.toString();
	}

	public static Node<ZIndex> formatZIndexNumber(Node<ZIndex> root) {
		return Funcs._formatZIndexNumber(root, new LinkedIntArray(10));
	}

	private static Node<ZIndex> _formatZIndexNumber(Node<ZIndex> node, LinkedIntArray nums) {
		node.get().setNumbers(nums.toArray());
		if (node.hasChild()) {
			int i = 0;
			nums.push(i);
			for (Node<ZIndex> child : node.getChildren()) {
				nums.setLast(i++);
				_formatZIndexNumber(child, nums);
			}
			nums.popLast();
		}
		return node;
	}

}
