package ufsm.comunicacao.t2.layer;


public abstract class StartLayer extends ConfirmationLayer {

	@Override
	protected void setPrevLayer(Layer l) {
	}

	public abstract boolean isAlive();

}
