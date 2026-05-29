package com.genersoft.iot.vmp.media.rtvs;

import com.genersoft.iot.vmp.gb28181.transmit.cmd.impl.SIPCommander;
import com.genersoft.iot.vmp.service.IPlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping(value = "/gbs/RTVS")
public class RTVSGatewayController {

    @Autowired
    private SIPCommander sipCommander;

    @RequestMapping("/**")
    public String test(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String sdp = parameterMap.get("SDP")[0];
        byte[] decode = Base64.getDecoder().decode(sdp);
        String s = new String(decode);
        System.out.println(s);
        System.out.println(request);
//        sipCommander.playStreamCmd();
//        playService.play()
        return "-1";
    }
}
