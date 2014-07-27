package recognition;

public enum Word
{
	PLAY, STOP, PIANO, FORTE, WG, ELECTRONIC;
	
	public WordType type()
	{
		switch(this)
		{
		case FORTE:
		case PIANO:
			return WordType.FADER;
		case PLAY:
		case STOP:
			return WordType.ORDER;
		case WG:
		case ELECTRONIC:
			return WordType.DESTINATION;
		}
		return null;
	}
}
