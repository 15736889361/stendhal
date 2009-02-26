package games.stendhal.client.gui.tradingcenter;

import games.stendhal.common.Constants;

import javax.swing.JFrame;

import marauroa.common.game.RPObject;

public class OrderPanelTest {
	
	public static void main(final String[] args) {
		final RPObject offer =  new RPObject();
		offer.put(Constants.ACCEPT_OFFER_PRICE, -1);
		final String rps = Constants.OFFER_GOODS;
		offer.addSlot(rps);
		final RPObject rpoItem = new RPObject();
		rpoItem.put("name", "axt");
		offer.getSlot(Constants.OFFER_GOODS).add(rpoItem);
		
		final OrderPanelController opc = new OrderPanelController(offer);
		final JFrame jf = new JFrame();
		jf.add(opc.getComponent());
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
    }
	
}
