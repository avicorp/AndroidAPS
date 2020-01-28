package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.Random;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.AssignAddressCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.VersionResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSetupState;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodStateChangedHandler;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmnipodConst;

public class AssignAddressAction implements OmnipodAction<PodSessionState> {
    private final int address;
    private final PodStateChangedHandler podStateChangedHandler;

    public AssignAddressAction(PodStateChangedHandler podStateChangedHandler) {
        this.address = generateRandomAddress();
        this.podStateChangedHandler = podStateChangedHandler;
    }

    private static int generateRandomAddress() {
        return 0x1f000000 | (new Random().nextInt() & 0x000fffff);
    }

    @Override
    public PodSessionState execute(OmnipodCommunicationService communicationService) {
        PodSetupState setupState = new PodSetupState(address, 0x00, 0x00);

        AssignAddressCommand assignAddress = new AssignAddressCommand(setupState.getAddress());
        OmnipodMessage assignAddressMessage = new OmnipodMessage(OmnipodConst.DEFAULT_ADDRESS,
                Collections.singletonList(assignAddress), setupState.getMessageNumber());

        VersionResponse assignAddressResponse = communicationService.exchangeMessages(VersionResponse.class, setupState, assignAddressMessage,
                OmnipodConst.DEFAULT_ADDRESS, setupState.getAddress());

        DateTimeZone timeZone = DateTimeZone.getDefault();

        PodSessionState podState = new PodSessionState(timeZone, address, assignAddressResponse.getPiVersion(),
                assignAddressResponse.getPmVersion(), assignAddressResponse.getLot(), assignAddressResponse.getTid(),
                setupState.getPacketNumber(), 0x00); // At this point, for an unknown reason, the pod starts counting messages from 0 again

        podState.addStateChangedHandler(podStateChangedHandler);
        return podState;
    }
}
