package ufsm.comunicacao.t2.layer;

public abstract class EndLayer extends Layer{
	
	@Override
	public Layer setNextLayer(Layer l) {
		return this;
	}

	@Override
	public Layer getLastLayer() {
		return this;
	}

	
	@Override
	public Layer addLayer(Layer l) {
		getPrevLayer().setNextLayer(l);
		if(l instanceof EndLayer) {
			return l;
		}		
		l.setNextLayer(l);
		return l;
	}
	
}
