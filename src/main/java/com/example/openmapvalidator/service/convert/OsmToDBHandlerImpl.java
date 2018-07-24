package com.example.openmapvalidator.service.convert;

import com.example.openmapvalidator.helper.ConfigurationService;
import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.helper.StreamWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
/**
 * @author Sanan.Ahmadzada
 */
@Component
public class OsmToDBHandlerImpl implements OsmToDBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsmToDBHandlerImpl.class);

    private final ConfigurationService configurationService;

    public OsmToDBHandlerImpl(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void handle(String fileName) {

        boolean isWindows = System.getProperty(Const.OS_NAME)
                .toLowerCase().startsWith(Const.OS_WINDOWS_NAME);

        StreamWrapper error, output;

        try {

            String OSM_ROOT;
            String cmd;

            String root = new File(System.getProperty("user.dir")).getAbsolutePath();


            if (isWindows) {
                OSM_ROOT = Const.OSM_WINDOWS_ROOT;

                String path = root + OSM_ROOT;

                cmd = path + File.separator +
                        Const.OSM_COMMAND + Const.SPACE + Const.OSM_COMMAND_CREATE_OPTION + Const.SPACE +
                        Const.OSM_COMMAND_USERNAME_OPTION + Const.SPACE + configurationService.getPSQL_USERNAME() +
                        Const.SPACE + Const.OSM_COMMAND_DATABASE_OPTION + Const.SPACE + configurationService.getOSM_COMMAND_DATABASE_ARGUMENT() +
                        Const.SPACE + Const.OSM_DEFAULT_STYLE_OPTION + Const.SPACE + path + File.separator + Const.OSM_STYLE + Const.SPACE +
                        new ClassPathResource(Const.MAP_FOLDER_ROOT).getFile().getAbsolutePath() + File.separator + fileName;

            } else {
                OSM_ROOT = Const.OSM_UNIX_ROOT;

                String path = root + OSM_ROOT;

                cmd = path + File.separator + Const.OSM_COMMAND + Const.SPACE + Const.OSM_COMMAND_CREATE_OPTION +
                        Const.SPACE + Const.OSM_COMMAND_DATABASE_OPTION + Const.SPACE + configurationService.getOSM_COMMAND_DATABASE_ARGUMENT() +
                        Const.SPACE + new ClassPathResource(Const.MAP_FOLDER_ROOT).getFile().getAbsolutePath() + File.separator + fileName;
            }

            LOGGER.info(cmd);

            Runtime rt = Runtime.getRuntime();


            Process proc = rt.exec(cmd);

            error = new StreamWrapper(proc.getErrorStream(), "ERROR");
            output = new StreamWrapper(proc.getInputStream(), "OUTPUT");
            int exitVal;

            error.start();
            output.start();
            error.join(3000);
            output.join(3000);

            LOGGER.debug("isAlive before ? : {}", proc.isAlive());

            exitVal = proc.waitFor();
            LOGGER.info("exitVal: {}\nOutput: {}\nError: {}", exitVal, output.getMessage(), error.getMessage());

            if (exitVal != 0) {
                LOGGER.error("Please resolve error");
                System.exit(1);
            }
            Thread.sleep(5000);
            LOGGER.debug("isAlive? : {}", proc.isAlive());
            proc.destroy();
        } catch(Exception e) {
            LOGGER.error(e.toString());
            e.printStackTrace();
            System.exit(-1);
        } finally {

        }

    }

}
