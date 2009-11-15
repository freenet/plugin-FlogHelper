/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.floghelper.ui;

import freenet.client.async.DatabaseDisabledException;
import freenet.pluginmanager.PluginNotFoundException;
import plugins.floghelper.data.DataFormatter;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.Base64;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import java.io.IOException;
import java.net.URI;
import plugins.floghelper.FlogHelper;
import plugins.floghelper.data.Activelink;
import plugins.floghelper.data.Flog;
import plugins.floghelper.data.pluginstore.PluginStoreFlog;
import plugins.floghelper.ui.flog.FlogFactory;

/**
 * This toadlet shows the list of all the flogs, it's also the main index
 * page of the plugin.
 *
 * @author Artefact2
 */
public class FlogListToadlet extends FlogHelperToadlet {

	public static final String MY_URI = "/";

	public FlogListToadlet(final HighLevelSimpleClient hlsc) {
		super(hlsc, MY_URI);
	}

	public void getPageGet(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		final HTMLNode table = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("FlogList"), pageNode.content).addChild("table");

		final HTMLNode tHead = table.addChild("thead");
		final HTMLNode tFoot = table.addChild("tfoot");
		final HTMLNode tBody = table.addChild("tbody");

		final HTMLNode actionsRow = new HTMLNode("tr");

		final HTMLNode formCreateNew = FlogHelper.getPR().addFormChild(actionsRow.addChild("th", "colspan", "12"), FlogHelperToadlet.BASE_URI +
				CreateOrEditFlogToadlet.MY_URI, "CreateNewFlog");
		formCreateNew.addAttribute("method", "get");
		formCreateNew.addChild("input", new String[]{"type", "value"},
				new String[]{"submit", FlogHelper.getBaseL10n().getString("CreateFlog")});

		final HTMLNode formImport = FlogHelper.getPR().addFormChild(actionsRow.addChild("th"), FlogHelperToadlet.BASE_URI +
				ImportFlogToadlet.MY_URI, "ImportFlog");
		formImport.addAttribute("method", "get");
		formImport.addChild("input", new String[]{"type", "value"},
				new String[]{"submit", FlogHelper.getBaseL10n().getString("Import")});

		final HTMLNode headersRow = new HTMLNode("tr");
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("ID"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Activelink"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Title"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("Author"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("SmallDescription"));
		headersRow.addChild("th", FlogHelper.getBaseL10n().getString("NumberOfEntries"));
		headersRow.addChild("th", "colspan", "7", FlogHelper.getBaseL10n().getString("Actions"));

		tHead.addChild(actionsRow);
		tHead.addChild(headersRow);
		tFoot.addChild(headersRow);

		if (FlogHelper.getStore().subStores.isEmpty()) {
			tBody.addChild("tr").addChild("td", "colspan", "13", FlogHelper.getBaseL10n().getString("NoFlogsYet"));
		}

		for (final Flog flog : PluginStoreFlog.getFlogs()) {
			final String author = flog.getAuthorName();

			final HTMLNode activelinkP = new HTMLNode("td");
			if (flog.hasActivelink()) {
				HTMLNode activelinkImg = new HTMLNode("img");
				final String base64str = Base64.encodeStandard(flog.getActivelink());
				activelinkImg.addAttribute("src", "data:" + Activelink.MIMETYPE + ";base64," + base64str);
				activelinkImg.addAttribute("width", Integer.toString(Activelink.WIDTH));
				activelinkImg.addAttribute("height", Integer.toString(Activelink.HEIGHT));
				activelinkImg.addAttribute("alt", FlogHelper.getBaseL10n().getString("ActivelinkAlt"));
				activelinkImg.addAttribute("style", "vertical-align: middle;");
				activelinkP.addChild(activelinkImg);
			}

			final HTMLNode row = tBody.addChild("tr");
			row.addChild("td").addChild("pre", DataFormatter.toString(flog.getID()));
			row.addChild(activelinkP);
			row.addChild("td", DataFormatter.toString(flog.getTitle()));
			row.addChild("td", DataFormatter.toString(author));
			row.addChild("td", flog.getShortDescription());
			row.addChild("td", DataFormatter.toString(flog.getNumberOfContents()));

			final HTMLNode formDetails = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					ContentListToadlet.MY_URI, "FlogDetails-" + flog.getID());
			formDetails.addAttribute("method", "get");
			formDetails.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Entries")});
			formDetails.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});

			final HTMLNode formPreview = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					PreviewToadlet.MY_URI + flog.getID() + "/index.html", "PreviewFlog-" + flog.getID());
			formPreview.addAttribute("method", "get");
			formPreview.addChild("input", new String[]{"type", "value", "name"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Preview"), "Preview"});

			final HTMLNode formDelete = FlogHelper.getPR().addFormChild(row.addChild("td"), this.path(),
					"DeleteFlog-" + flog.getID());
			formDelete.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Delete")});
			formDelete.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogToDelete", DataFormatter.toString(flog.getID())});

			final HTMLNode formEdit = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					CreateOrEditFlogToadlet.MY_URI, "EditFlog-" + flog.getID());
			formEdit.addAttribute("method", "get");
			formEdit.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Edit")});
			formEdit.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});

			final HTMLNode formInsert = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					FlogListToadlet.MY_URI, "InsertFlog-" + flog.getID());
			formInsert.addAttribute("method", "post");
			formInsert.addChild("input", new String[]{"type", "value", "name"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Insert"), "Insert"});
			formInsert.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});

			final HTMLNode formAttachements = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					AttachmentsToadlet.MY_URI, "Attachements-" + flog.getID());
			formAttachements.addAttribute("method", "get");
			formAttachements.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Attachements")});
			formAttachements.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});

			final HTMLNode formExport = FlogHelper.getPR().addFormChild(row.addChild("td"), FlogHelperToadlet.BASE_URI +
					ExportFlogToadlet.MY_URI, "EditFlog-" + flog.getID());
			formExport.addAttribute("method", "get");
			formExport.addChild("input", new String[]{"type", "value"},
					new String[]{"submit", FlogHelper.getBaseL10n().getString("Export")});
			formExport.addChild("input", new String[]{"type", "name", "value"},
					new String[]{"hidden", "FlogID", DataFormatter.toString(flog.getID())});
		}

		// This is only debug code to see what is in the PluginStore.
		//this.getPM().getInfobox("infobox-minor", "DEBUG PluginStore Dump", pageNode.content).addChild("pre", DataFormatter.printStore(FlogHelper.getStore()));

		writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
	}

	public void getPagePost(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if (request.isPartSet("FlogToDelete") || request.isPartSet("FlogToReallyDelete")) {
			final String idToDelete = request.getPartAsString("FlogToDelete", 7);
			final String idToReallyDelete = request.getPartAsString("FlogToReallyDelete", 7);

			if (idToReallyDelete != null && !idToReallyDelete.equals("")) {
				if (request.getPartAsString("Yes", 3).equals("Yes")) {
					FlogHelper.getStore().subStores.remove(idToReallyDelete);
					FlogHelper.putStore();
					this.handleMethodGET(uri, request, ctx);
					return;
				} else {
					this.handleMethodGET(uri, request, ctx);
					return;
				}
			}

			if (idToDelete != null && !idToDelete.equals("")) {
				final HTMLNode confirm = this.getPM().getInfobox("infobox-alert", FlogHelper.getBaseL10n().getString("ReallyDelete"), pageNode.content);
				final HTMLNode form = FlogHelper.getPR().addFormChild(confirm, this.path(), "ReallyDelete-" + idToDelete);
				form.addChild("p", FlogHelper.getBaseL10n().getString("ReallyDeleteFlogLong").replace("${FlogID}", idToDelete));
				final HTMLNode buttons = form.addChild("p");
				buttons.addChild("input", new String[]{"type", "name", "value"},
						new String[]{"hidden", "FlogToReallyDelete", idToDelete});
				buttons.addChild("input", new String[]{"type", "name", "value"},
						new String[]{"submit", "Yes", FlogHelper.getBaseL10n().getString("Yes")});
				buttons.addChild("input", new String[]{"type", "name", "value"},
						new String[]{"submit", "No", FlogHelper.getBaseL10n().getString("No")});
			}

			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
		} else if(request.isPartSet("Insert") && request.isPartSet("FlogID")) {
			try {
				new FlogFactory(new PluginStoreFlog(request.getPartAsString("FlogID", 7))).insert();
			} catch (PluginNotFoundException ex) {
				// Won't happen
			} catch (DatabaseDisabledException ex) {
				// Won't happen
			}

			HTMLNode infobox = this.getPM().getInfobox(null, FlogHelper.getBaseL10n().getString("InsertInProgress"), pageNode.content);

			infobox.addChild("p", FlogHelper.getBaseL10n().getString("FlogIsInsertingLong"));
			HTMLNode links = infobox.addChild("p");
			links.addChild("strong").addChild("a", "href", "/uploads/", FlogHelper.getBaseL10n().getString("GoToInsertsPage"));
			links.addChild("br");
			links.addChild("a", "href", FlogHelperToadlet.BASE_URI + FlogListToadlet.MY_URI, FlogHelper.getBaseL10n().getString("ReturnToFlogList"));

			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
		}
	}
}
