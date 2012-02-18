package org.nutz.doc.html;

import static org.nutz.lang.util.Tag.tag;
import static org.nutz.lang.util.Tag.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.doc.meta.Author;
import org.nutz.doc.meta.ZBlock;
import org.nutz.doc.meta.ZDoc;
import org.nutz.doc.meta.ZEle;
import org.nutz.doc.meta.ZFont;
import org.nutz.doc.meta.ZIndex;
import org.nutz.doc.util.Funcs;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Node;
import org.nutz.lang.util.Tag;

public class HtmlRenderSupport {

	public static final String COMMON_INFO = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";

	private boolean skipAllIndexBlock;

	private boolean notShowTopInEachSection;

	public boolean isSkipAllIndexBlock() {
		return skipAllIndexBlock;
	}

	public void setSkipAllIndexBlock(boolean skipAllIndexBlock) {
		this.skipAllIndexBlock = skipAllIndexBlock;
	}

	public boolean isNotShowTopInEachSection() {
		return notShowTopInEachSection;
	}

	public void setNotShowTopInEachSection(boolean notShowTopInEachSection) {
		this.notShowTopInEachSection = notShowTopInEachSection;
	}

	protected static Tag appendAuthorTag(ZDoc doc, Tag ele) {
		appendAuthors(ele, "By:", doc.authors());
		appendAuthors(ele, "Verify by:", doc.verifiers());
		return ele;
	}

	protected static void appendAuthors(Tag ele, String prefix, Author[] authors) {
		if (authors.length > 0) {
			ele.add(Tag.tag("em").add(Tag.text(prefix)));
			for (Author au : authors) {
				String email = au.getEmailString();
				ele.add(Tag.tag("b").add(Tag.text(au.getName())));
				if (!Strings.isBlank(email))
					ele.add(Tag.tag("a")
								.attr("href", "mailto:" + email)
								.add(Tag.text("<" + email + ">")));
			}
		}
	}

	protected static final String[] OLTYPES = {"1", "a", "i"};

	protected void renderBlock(Tag parent, ZBlock block) {
		// <Table>
		if (block.isTable()) {
			Tag tab = tag("table").attr("border", "1");
			tab.attr("cellspacing", "2").attr("cellpadding", "4");
			ZBlock[] rows = block.children();
			for (ZBlock row : rows) {
				Tag tagTr = tag("tr");
				for (ZBlock td : row.children()) {
					tagTr.add(renderToHtmlBlockElement(tag("td"), td.eles()));
				}
				tab.add(tagTr);
			}
			parent.add(tab);
		}
		// <Hr>
		else if (block.isHr()) {
			// parent.add(tag("hr"));
			parent.add(Tag.tag("div", ".hr").add(Tag.tag("b")));
		}
		// #index:
		else if (block.isIndexRange()) {
			if (!skipAllIndexBlock) {
				// parent.add(renderIndexTable(block.getDoc().buildIndex(block.getIndexRange())));
				Node<ZIndex> indexRoot = block.getDoc().root().buildIndex(block.getIndexRange());
				Tag indexTable = renderIndexTable(indexRoot);
				if (null != indexTable)
					parent.add(indexTable);
			}
		}
		// <OL>
		else if (block.isOL()) {
			Tag tag = tag("ol");
			for (ZBlock li : block.children()) {
				renderListItem(tag, li);
			}
			parent.add(tag);
		}
		// <UL>
		else if (block.isUL()) {
			Tag tag = tag("ul");
			for (ZBlock li : block.children()) {
				renderListItem(tag, li);
			}
			parent.add(tag);
		}
		// <Pre>
		else if (block.isCode()) {
			Tag pre = tag("pre");
			String[] lines = block.getText().split("[\r]?\n");
			for (String line : lines) {
				// 替换注释行
				// TODO 让 tag 支持真正的 html 解析
				Matcher m = REG_CMT.matcher(line);
				if (m.find()) {
					String s = line.substring(0, m.start());
					String cmt = line.substring(m.start()) + "\n";
					pre.add(text(s)).add(tag("span").attr("class", "zdoc_code_cmt").add(text(cmt)));
				} else {
					pre.add(text(line + "\n"));
				}
			}
			// 加入 DOM
			parent.add(pre);
		}
		// <H1~6>
		else if (block.isHeading()) {
			renderHeading(parent, block);
			ZBlock[] ps = block.children();
			for (ZBlock p : ps)
				renderBlock(parent, p);
		}
		// <P>
		else {
			parent.add(renderToHtmlBlockElement(tag("p"), block.eles()));
		}
	}

	private static final Pattern REG_CMT = Pattern.compile("(//|#)(.*)$");

	public Tag renderIndexTable(Node<ZIndex> indexRoot) {
		if (!indexRoot.hasChild())
			return null;
		Tag tag = null;
		if (indexRoot.firstChild().get().hasNumbers())
			tag = tag("ul");
		else
			tag = tag("ol");
		tag.attr("class", "zdoc_index_table");
		for (Node<ZIndex> indexNode : indexRoot.getChildren()) {
			tag.add(renderIndex(indexNode));
		}
		return tag;
	}

	Tag renderIndex(Node<ZIndex> node) {
		ZIndex index = node.get();
		Tag div = tag("div");
		Tag li = (Tag) tag("li").add(div);
		// Nubmers
		if (index.hasNumbers()) {
			div.add(tag("span").attr("class", "num").add(text(index.getNumberString())));
		}

		// Text & Href
		if (index.getHref() != null)
			div.add(tag("a").attr("href", index.getHref()).add(text(index.getText())));
		else
			div.add(tag("b").add(text(index.getText())));
		// Children
		if (node.hasChild()) {
			div.attr("class", "zdoc_folder");
			Tag ul = tag("ul");
			for (Node<ZIndex> sub : node.getChildren())
				ul.add(renderIndex(sub));
			li.add(ul);
		}
		return li;
	}

	private static final String[] ULTYPES = {"disc", "circle", "square"};

	protected static Tag wrapFont(Tag tag, String tagName, boolean yes) {
		if (yes)
			return (Tag) tag(tagName).add(tag);
		return tag;
	}

	protected void renderListItem(Tag tag, ZBlock li) {
		int liDeep = li.countMyTypeInAncestors();
		String[] ss = li.isULI() ? ULTYPES : OLTYPES;
		tag.attr("type", ss[liDeep % ss.length]);
		Tag liTag = renderToHtmlBlockElement(tag("li"), li.eles());
		if (li.hasChildren()) {
			ZBlock[] ps = li.children();
			for (ZBlock p : ps)
				renderBlock(liTag, p);
		}
		tag.add(liTag);
	}

	protected void renderHeading(Tag parent, ZBlock block) {
		Tag hn = tag("h" + (block.depth()));
		hn.add(tag("a").attr("name", Funcs.evalAnchorName(block.getText())));
		parent.add(renderToHtmlBlockElement(hn, block.eles()));
		// 显示 #top 链接
		if (!notShowTopInEachSection) {
			Tag div = tag("div").attr("style", "float:right;");
			div.add(tag("a").attr("href", "#top").add(text("Top")));
			parent.add(div);
		}
	}

	protected Tag renderParagraph(ZBlock p) {
		return renderToHtmlBlockElement(tag("p"), p.eles());
	}

	protected Tag renderToHtmlBlockElement(Tag tag, ZEle[] eles) {
		for (ZEle ele : eles) {
			Tag sub = renderEle(ele);
			if (null != sub)
				tag.add(sub);
		}
		return tag;
	}

	protected Tag renderEle(ZEle ele) {
		Tag tag = null;
		if (ele.isImage()) {
			String src;
			if (ele.getSrc().isWWW())
				src = ele.getSrc().getPath();
			else
				src = ele.getSrc().getValue();
			tag = tag("img").attr("src", src);
			if (ele.getWidth() > 0)
				tag.attr("width", ele.getWidth());
			if (ele.getHeight() > 0)
				tag.attr("height", ele.getHeight());
			if (ele.hasHref() && !ele.getHref().isBookmark())
				tag = (Tag) tag("a").attr("href", ele.getHref().getPath()).add(tag);
			return tag;
		} else if (!Strings.isEmpty(ele.getText())) {
			tag = text(ele.getText());
			if (ele.hasStyle() && ele.getStyle().hasFont()) {
				ZFont font = ele.getStyle().getFont();
				tag = wrapFont(tag, "b", font.isBold());
				tag = wrapFont(tag, "i", font.isItalic());
				tag = wrapFont(tag, "s", font.isStrike());
				tag = wrapFont(tag, "sub", font.isSub());
				tag = wrapFont(tag, "sup", font.isSup());
				tag = wrapFontColor(tag, font);
			}
		}
		// 纯书签
		if (null == tag) {
			if (ele.hasHref() && ele.getHref().isBookmark())
				return tag("a").attr("name", ele.getHref().getValue());
			return tag;
		}
		// 加链接
		if (ele.hasHref())
			if (ele.getHref().isAvailable())
				tag = (Tag) tag("a").attr("href", ele.getHref().getPath()).add(tag);
		return tag;
	}

	protected static Tag wrapFontColor(Tag tag, ZFont font) {
		if (font.hasColor())
			return (Tag) tag("span").attr("style", "color:" + font.getColor() + ";").add(tag);
		return tag;
	}

}