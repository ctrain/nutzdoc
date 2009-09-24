package org.nutz.doc.meta;

public class ZDocs {

	public static ZBlock p(String text) {
		ZBlock p = p();
		p.append(ele(text));
		return p;
	}

	public static ZBlock p() {
		return new ZBlock();
	}

	public static ZEle ele(String text) {
		return new ZEle(text);
	}

	public static ZRefer refer(String str) {
		ZRefer r = new ZRefer();
		r.setPath(str);
		return r;
	}

	public static ZColor color(String str) {
		return new ZColor(str);
	}

	public static ZColor color() {
		return new ZColor();
	}

	public static Author author(String str) {
		return new Author(str);
	}

}