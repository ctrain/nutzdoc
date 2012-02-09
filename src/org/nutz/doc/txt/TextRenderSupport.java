package org.nutz.doc.txt;

import org.nutz.doc.meta.Author;
import org.nutz.doc.meta.ZBlock;
import org.nutz.doc.meta.ZDoc;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Tag;

public class TextRenderSupport {

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

	protected void renderBlock(StringBuilder sb, ZBlock block) {
		// <Table>
		if (block.isTable()) {
			sb.append("\n");
			ZBlock[] rows = block.children();
			for (ZBlock row : rows) {
				for (ZBlock td : row.children()) {
					sb.append(" - ").append(td.getText());
				}
				sb.append("\n\n");
			}
		}
		// <Hr>
		else if (block.isHr()) {
			// parent.add(tag("hr"));
			sb.append("\n");
			sb.append(Strings.dup('-', 70));
		}
		// #index:
		else if (block.isIndexRange()) {}
		// <OL>
		else if (block.isOL()) {
			sb.append("\n");
			int i = 0;
			for (ZBlock li : block.children()) {
				if (Strings.isBlank(li.getText()))
					continue;
				renderListItem(i++, sb, li);
			}
			sb.append("\n");
		}
		// <UL>
		else if (block.isUL()) {
			sb.append("\n");
			for (ZBlock li : block.children()) {
				renderListItem(-1, sb, li);
			}
			sb.append("\n");
		}
		// <Pre>
		else if (block.isCode()) {
			sb.append(String.format("\n    %s <%s> ~~\n",
									Strings.dup('~', 50),
									Strings.sBlank(block.getTitle(), "Code")));
			sb.append("    " + block.getText().replaceAll("\n", "\n    "));
			sb.append(Strings.dup('~', 60)).append('\n');
		}
		// <H1~6>
		else if (block.isHeading()) {
			sb.append("\n");
			sb.append(Strings.dup('=', block.depth()));
			sb.append(" ").append(block.getText()).append("\n");
			ZBlock[] ps = block.children();
			for (ZBlock p : ps)
				renderBlock(sb, p);
		}
		// <P>
		else {
			if (!Strings.isBlank(block.getText())) {
				sb.append("\n");
				sb.append("    " + block.getText().replaceAll("\n", "\n    "));
				sb.append("\n");
			}
		}
	}

	protected void renderListItem(int index, StringBuilder sb, ZBlock li) {
		sb.append(Strings.dup("  ", li.depth()));
		sb.append(li.isOLI() ? " " + Strings.alignRight((index + 1), 2, ' ') + ". " : " * ");
		sb.append(li.getText());
		sb.append("\n");
	}

}
