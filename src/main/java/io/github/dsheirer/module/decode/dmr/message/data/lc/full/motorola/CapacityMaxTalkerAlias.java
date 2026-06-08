package io.github.dsheirer.module.decode.dmr.message.data.lc.full.motorola;

import io.github.dsheirer.bits.CorrectedBinaryMessage;
import io.github.dsheirer.identifier.Identifier;
import io.github.dsheirer.identifier.alias.DmrTalkerAliasIdentifier;
import io.github.dsheirer.module.decode.dmr.message.type.TalkerAliasDataFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Capacity Max Talker Alias (FLCO 20 / FID 0x10)
 * <p>
 * See also: FLCO 21 - Talker Alias Continuation
 */
public class CapacityMaxTalkerAlias extends CapacityPlusVoiceChannelUser
{
    //Bit layout mirrors the standard ETSI talker alias header (FLCO 0x04), just with vendor FID 0x10.
    private static final int[] FORMAT = new int[]{16, 17};
    private static final int[] LENGTH = new int[]{18, 19, 20, 21, 22};
    private static final int ALIAS_START = 23;
    private static final int ALIAS_END = 72;
    private DmrTalkerAliasIdentifier mTalkerAliasIdentifier;
    private List<Identifier> mIdentifiers;

    /**
     * Constructs an instance.
     *
     * @param message for the link control payload
     * @param timestamp
     * @param timeslot
     */
    public CapacityMaxTalkerAlias(CorrectedBinaryMessage message, long timestamp, int timeslot)
    {
        super(message, timestamp, timeslot);
    }

    /**
     * Encoding format for the alias characters, mirroring the standard ETSI talker alias header format field.
     */
    public TalkerAliasDataFormat getFormat()
    {
        int value = getMessage().getInt(FORMAT);

        try
        {
            return TalkerAliasDataFormat.fromValue(value);
        }
        catch(Exception e)
        {
            return TalkerAliasDataFormat.UTF_8;
        }
    }

    /**
     * Total alias length in characters.
     */
    public int getLength()
    {
        return getMessage().getInt(LENGTH);
    }

    /**
     * Raw alias payload bits carried by this header message.  Note: this fragment must be combined with any
     * continuation message fragment(s) and decoded as a single value -- decoding this fragment in isolation can
     * split a multi-byte (e.g. UTF-8) character across the fragment boundary and corrupt the decoded text.
     *
     * @return alias payload fragment.
     */
    public CorrectedBinaryMessage getAliasFragment()
    {
        return getMessage().getSubMessage(ALIAS_START, ALIAS_END);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        if(!isValid())
        {
            sb.append("[CRC-ERROR] ");
        }
        if(isEncrypted())
        {
            sb.append(" *ENCRYPTED*");
        }
        if(isReservedBitSet())
        {
            sb.append(" *RESERVED-BIT*");
        }

        sb.append("FLC MOTOROLA CAPMAX TALKER ALIAS:").append(getTalkerAliasIdentifier());
        sb.append(" FORMAT:").append(getFormat());
        sb.append(" LENGTH:").append(getLength());
        sb.append(" MSG:").append(getMessage().toHexString());
        return sb.toString();
    }

    /**
     * Talker alias identifier
     *
     * @return identifier
     */
    public DmrTalkerAliasIdentifier getTalkerAliasIdentifier()
    {
        if(mTalkerAliasIdentifier == null)
        {
            String alias = new String(getMessage().get(ALIAS_START, ALIAS_END).getBytes()).trim();
            mTalkerAliasIdentifier = DmrTalkerAliasIdentifier.create(alias);
        }

        return mTalkerAliasIdentifier;
    }

    @Override
    public List<Identifier> getIdentifiers()
    {
        if(mIdentifiers == null)
        {
            mIdentifiers = new ArrayList<>();
            mIdentifiers.add(getTalkerAliasIdentifier());
        }

        return mIdentifiers;
    }
}
