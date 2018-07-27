package com.example.openmapvalidator.service.convert;

import com.example.openmapvalidator.helper.ConfigurationService;
import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.helper.StreamGobbler;
import com.example.openmapvalidator.helper.StreamWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.Executors;

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

            ProcessBuilder builder = new ProcessBuilder();

            if (isWindows) {
                OSM_ROOT = Const.OSM_WINDOWS_ROOT;

                String path = root + OSM_ROOT;

                builder.command(Const.OSM_COMMAND, "-c",
                        "-d", configurationService.getOSM_COMMAND_DATABASE_ARGUMENT(),
                        Const.OSM_COMMAND_USERNAME_OPTION, configurationService.getPSQL_USERNAME(),
                        Const.OSM_DEFAULT_STYLE_OPTION, path + File.separator + Const.OSM_STYLE,
                        new ClassPathResource(Const.MAP_FOLDER_ROOT).getFile().getAbsolutePath() + File.separator + fileName);

            } else {
                OSM_ROOT = Const.OSM_UNIX_ROOT;

                String path = root + OSM_ROOT;

                cmd = path + File.separator + Const.OSM_COMMAND + Const.SPACE + Const.OSM_COMMAND_CREATE_OPTION +
                        Const.SPACE + Const.OSM_COMMAND_DATABASE_OPTION + Const.SPACE + configurationService.getOSM_COMMAND_DATABASE_ARGUMENT() +
                        Const.SPACE + new ClassPathResource(Const.MAP_FOLDER_ROOT).getFile().getAbsolutePath() + File.separator + fileName;
            }
//
            /*Process proc = rt.exec(cmd);

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
            LOGGER.debug("isAlive? : {}", proc.isAlive());*/
            //proc.destroy();

            cmd = "C:\\dev\\projects\\map\\bashscript\\windows\\osm2pgsql-bin\\osm2pgsql -c -d map-db -U postgres " +
                    "-S C:\\dev\\projects\\map\\bashscript\\windows\\osm2pgsql-bin\\default.style " +
                    "C:\\dev\\projects\\map\\target\\classes\\map\\bostonmap.osm";


            LOGGER.debug("------");
            LOGGER.debug(Arrays.toString(builder.command().toArray()));


            //builder.command("cmd.exe", "/c", "dir");
            String workingDir = System.getProperty("user.dir");
            File file = new File(workingDir + File.separator + "bashscript" + File.separator + "windows" - File.separator +
                    "osm2pgsql-bin" + File.separator);
            builder.directory(file);

            Process process = builder.start();
            int exitCode = process.waitFor();

            LOGGER.debug("Echo command executed, any errors? " + (exitCode == 0 ? "No" : "Yes"));

            LOGGER.debug("Echo Output:\n" + output(process.getInputStream()));

            assert exitCode == 0;

        } catch(Exception e) {
            LOGGER.error(e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

    }

    private static String output(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {

            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        return sb.toString();
    }


}
