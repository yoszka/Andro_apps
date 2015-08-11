package tomasz.jokiel.blootothcontroller.iodevice;

/**
 * Example of command *:ACK&ADC&165:#
 */
public class CommandByteParser implements ByteParser{
    private static final String COMMAND_START = "*:";
    private static final byte COMMAND_START_1 = '*';
    private static final byte COMMAND_START_2 = ':';
    private static final String COMMAND_END = ":#";
    private static final byte COMMAND_END_1 = ':';
    private static final byte COMMAND_END_2 = '#';

    private static final int COMMAND_BUFFER_SIZE = 20;
    private static final int MINIMUM_DATA_RECEIVED_COUNT = 5;

    private byte[] mReceiverBytes = new byte[COMMAND_BUFFER_SIZE];
    private byte mPreviousByte = 0;
    private int mReceiverBytesCounter = 0;
    private final CommandReceivedListener mCommandReceivedListener;

    public CommandByteParser(CommandReceivedListener commandReceivedListener) {
        mCommandReceivedListener = commandReceivedListener;
    }

    @Override
    public void addByteToParse(byte currentByte) {

        if (isFrameStart(currentByte)) {
            reset();
            mReceiverBytes[mReceiverBytesCounter++] = mPreviousByte;
        }

        if (isInsideCommand()) {
            mReceiverBytes[mReceiverBytesCounter++] = currentByte;

            if (isBufferFull()) {
                mReceiverBytesCounter--;
            }
        }

        if (isFrameEnd(currentByte)) {
            mReceiverBytes[mReceiverBytesCounter] = 0x00;
            reset();

            String receivedCommand = new String(mReceiverBytes).trim();

            if (isCommandString(receivedCommand)) {
                mCommandReceivedListener.onCommandReceived(receivedCommand);
            }
        }

        mPreviousByte = currentByte;
    }

    private boolean isBufferFull() {
        return mReceiverBytesCounter == COMMAND_BUFFER_SIZE;
    }

    private boolean isInsideCommand() {
        return mReceiverBytesCounter > 0;
    }

    private boolean isFrameStart(byte currentByte) {
        return (mPreviousByte == COMMAND_START_1) && (currentByte == COMMAND_START_2);
    }

    private boolean isFrameEnd(byte currentByte) {
        return (mPreviousByte == COMMAND_END_1) && (currentByte == COMMAND_END_2)
                && (mReceiverBytesCounter >= MINIMUM_DATA_RECEIVED_COUNT);
    }

    private boolean isCommandString(String receivedCommand) {
        return receivedCommand.startsWith(COMMAND_START) && receivedCommand.endsWith(COMMAND_END);
    }

    private void reset() {
        mReceiverBytesCounter = 0;
    }

    public interface CommandReceivedListener {
        public void onCommandReceived(String receivedCommand);
    }
}
