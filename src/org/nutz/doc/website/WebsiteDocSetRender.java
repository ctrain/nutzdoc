package org.nutz.doc.website;

import java.io.IOException;
import org.nutz.doc.DocSetRender;
import org.nutz.doc.RenderLogger;
import org.nutz.doc.meta.ZDocSet;

/**
 * 将一个 zDoc 的目录渲染成一个网站，网站需要带菜单条（第一层目录）<br>
 * 之后每个二级目录，作为频道的左侧快速导航，二级目录的子目录可以展开
 * <p>
 * 同普通 html 渲染方式不同的是，这个渲染器，仅仅渲染 index.tmpl 文件
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WebsiteDocSetRender implements DocSetRender {

	private RenderLogger L;

	public WebsiteDocSetRender(RenderLogger L) {
		this.L = L;
	}

	@Override
	public void render(String destPath, ZDocSet set) throws IOException {
		new WebsiteRendering(destPath, set, L).render();
	}

}
