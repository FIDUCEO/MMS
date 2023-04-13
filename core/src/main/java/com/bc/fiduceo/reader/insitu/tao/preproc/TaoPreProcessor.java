package com.bc.fiduceo.reader.insitu.tao.preproc;

import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StringUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class TaoPreProcessor {

    public static void main(String[] args) throws IOException {
        final Configuration configuration = new Configuration();
        configuration.sourceDir = "C:\\Satellite\\CIMR\\TAO_buoy";
        configuration.targetDir = "C:\\Satellite\\CIMR\\TAO_merged";
        configuration.filePrefix = "TAO_T2N180W";
        configuration.sssFileName = "TAO_T2N180W_D_SALT_hourly.ascii";
        configuration.sstFileName = "TAO_T2N180W_D_SST_10min.ascii";
        configuration.airtFileName = "TAO_T2N180W_D_AIRT_10min.ascii";
        configuration.rhFileName = "TAO_T2N180W_D_RH_10min.ascii";
        configuration.windFileName = "TAO_T2N180W_D_WIND_10min.ascii";
        configuration.baroFileName = null;
        configuration.rainFileName = null;
        configuration.posFileName = "TAO_T2N180W_R_POS.ascii";
        configuration.nominalLon = -180.f;
        configuration.nominalLat = 2.f;

        // --- read all we need ---
        final File sourceDir = new File(configuration.sourceDir);
        final File sssFile = new File(sourceDir, configuration.sssFileName);

        final HashMap<String, List<SSSRecord>> sssMap = parseSSSFile(sssFile);
        final SSTProvider sstProvider = new SSTProvider();
        sstProvider.open(getFile(sourceDir, configuration.sstFileName));

        final AirtProvider airtProvider = new AirtProvider();
        airtProvider.open(getFile(sourceDir, configuration.airtFileName));

        final RHProvider rhProvider = new RHProvider();
        rhProvider.open(getFile(sourceDir, configuration.rhFileName));

        final WINDProvider windProvider = new WINDProvider();
        windProvider.open(getFile(sourceDir, configuration.windFileName));

        final BAROProvider baroProvider = new BAROProvider();
        baroProvider.open(getFile(sourceDir, configuration.baroFileName));

        final RAINProvider rainProvider = new RAINProvider();
        rainProvider.open(getFile(sourceDir, configuration.rainFileName));

        final POSProvider posProvider = new POSProvider();
        posProvider.open(getFile(sourceDir, configuration.posFileName), configuration.nominalLon, configuration.nominalLat);

        final HashMap<String, List<TAORecord>> taoMap = new HashMap<>();

        // --- assemble final records ---
        Set<String> deployments = sssMap.keySet();
        for (final String deployment : deployments) {
            final List<SSSRecord> sssRecords = sssMap.get(deployment);
            final ArrayList<TAORecord> taoRecords = new ArrayList<>();

            for (final SSSRecord sssRecord : sssRecords) {
                final TAORecord taoRecord = new TAORecord();
                String M = "";
                String Q = "";

                // date and place
                taoRecord.date = sssRecord.date;
                final POSRecord posRecord = posProvider.get(taoRecord.date);
                taoRecord.lon = Float.toString(posRecord.lon);
                taoRecord.lat = Float.toString(posRecord.lat);

                // salinity
                taoRecord.SSS = sssRecord.SSS;
                M = M.concat(sssRecord.M);
                Q = Q.concat(sssRecord.Q);

                // sst data
                final SSTRecord sstRecord = sstProvider.get(sssRecord.date);
                taoRecord.SST = sstRecord.SST;
                M = M.concat(sstRecord.M);
                Q = Q.concat(sstRecord.Q);

                // airt data
                final AIRTRecord airtRecord = airtProvider.get(sssRecord.date);
                taoRecord.AIRT = airtRecord.AIRT;
                M = M.concat(airtRecord.M);
                Q = Q.concat(airtRecord.Q);

                // relative humidity
                final RHRecord rhRecord = rhProvider.get(sssRecord.date);
                taoRecord.RH = rhRecord.RH;
                M = M.concat(rhRecord.M);
                Q = Q.concat(rhRecord.Q);

                // wind
                final WINDRecord windRecord = windProvider.get(sssRecord.date);
                taoRecord.WSPD = windRecord.WSPD;
                taoRecord.WDIR = windRecord.WDIR;
                M = M.concat(windRecord.M);
                Q = Q.concat(windRecord.Q);

                // barometric pressure
                final BARORecord baroRecord = baroProvider.get(sssRecord.date);
                taoRecord.BARO = baroRecord.BARO;
                M = M.concat(baroRecord.M);
                Q = Q.concat(baroRecord.Q);

                // rainfall
                final RAINRecord rainRecord = rainProvider.get(sssRecord.date);
                taoRecord.RAIN = rainRecord.RAIN;
                M = M.concat(rainRecord.M);
                Q = Q.concat(rainRecord.Q);

                taoRecord.M = M;
                taoRecord.Q = Q;

                taoRecords.add(taoRecord);
            }

            taoMap.put(deployment, taoRecords);
        }

        // --- write files per month and deployment ----
        final DecimalFormat format = new DecimalFormat("00");
        deployments = taoMap.keySet();
        PrintWriter writer = null;
        for (final String deployment : deployments) {
            final Calendar utcCalendar = TimeUtils.getUTCCalendar();
            final String filePrefix = configuration.filePrefix + "_" + deployment + "_";

            int currentYear = -9;
            int currentMonth = -9;

            final List<TAORecord> taoRecords = taoMap.get(deployment);
            for (final TAORecord taoRecord : taoRecords) {
                utcCalendar.setTimeInMillis(taoRecord.date * 1000L);
                int year = utcCalendar.get(Calendar.YEAR);
                int month = utcCalendar.get(Calendar.MONTH) + 1;
                if (year != currentYear || month != currentMonth) {
                    currentYear = year;
                    currentMonth = month;
                    writer = switchWriter(configuration, format, writer, filePrefix, currentYear, currentMonth);
                }

                writer.println(taoRecord.toLine());
            }
        }

        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

    private static File getFile(File sourceDir, String fileName) {
        if (StringUtils.isNotNullAndNotEmpty(fileName)) {
            return new File(sourceDir, fileName);
        }
        return null;
    }

    private static PrintWriter switchWriter(Configuration configuration, DecimalFormat format, PrintWriter writer, String filePrefix, int currentYear, int currentMonth) throws IOException {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
        // create new writer
        final File targetDir = new File(configuration.targetDir, currentYear + File.separator + format.format(currentMonth));
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new IOException("unable to create directory: " + targetDir.getAbsolutePath());
            }
        }

        final File targetFile = new File(targetDir, filePrefix + currentYear + "-" + format.format(currentMonth) + ".txt");
        if (!targetFile.createNewFile()) {
            throw new IOException("unable to create file: " + targetFile.getAbsolutePath());
        }

        final PrintWriter printWriter = new PrintWriter(targetFile);
        printWriter.println("# utc longitude latitude SSS SST AIRT RH WSPD WDIR BARO RAIN M Q");
        return printWriter;
    }

    private static HashMap<String, List<SSSRecord>> parseSSSFile(File sssFile) {
        final HashMap<String, List<SSSRecord>> sssMap = new HashMap<>();
        try (final FileReader fileReader = new FileReader(sssFile)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            ArrayList<SSSRecord> records = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("Platform") || line.startsWith("Parameter") || line.startsWith("YYYY")) {
                    continue;   // skip these tb 2023-03-30
                }

                if (line.startsWith("Deployment")) {
                    // parse deployment and start new record-container
                    final String[] tokens = StringUtils.split(line, new char[]{' '}, true);
                    records = new ArrayList<>();
                    sssMap.put(tokens[1], records);
                    continue;
                }

                if (line.startsWith("Depth") || line.startsWith("Height")) {
                    // TODO! parse depth or height and store
                    continue;
                }

                final String[] tokens = TaoPreProcessor.tokenize(line);
                final SSSRecord sssRecord = new SSSRecord();
                sssRecord.date = toUnixEpoch(tokens[0], tokens[1]);
                sssRecord.SSS = tokens[2];
                if (tokens.length == 12) {
                    sssRecord.Q = tokens[10].substring(0, 1);
                    sssRecord.M = tokens[11].substring(0, 1);
                } else if (tokens.length == 9) {
                    sssRecord.Q = tokens[7].substring(0, 1);
                    sssRecord.M = tokens[8].substring(0, 1);
                } else if (tokens.length == 6) {
                    sssRecord.Q = tokens[4].substring(0, 1);
                    sssRecord.M = tokens[5].substring(0, 1);
                }else if (tokens.length == 5) {
                    sssRecord.Q = tokens[3];
                    sssRecord.M = tokens[4];
                } else {
                    throw new IOException("unknown line format:" + line);
                }

                records.add(sssRecord);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sssMap;
    }

    // Triton
    // ------
    // salt YYYYMMDD HHMMSS SSS Q M depth
    // airt AIRT Q M height
    // baro SLP Q M height
    // rain RAIN Q M height
    // rh RH Q M height
    // sst SST Q M depth
    // wind WSPD WDIR Q M height
    // lon
    // lat
    // 8 parameter

    // TAO
    // salt YYYYMMDD HHMMSS SSS Q M depth
    // airt AIRT Q M height
    // baro SLP Q M height
    // rain RAIN Q M height
    // rh RH Q M height
    // sst SST Q M depth
    // wind WSPD WDIR Q M height

    static int toUnixEpoch(String yyyymmdd, String hhmmss) {
        int year = Integer.parseInt(yyyymmdd.substring(0, 4));
        int month = Integer.parseInt(yyyymmdd.substring(4, 6));
        int day = Integer.parseInt(yyyymmdd.substring(6, 8));

        int hour = Integer.parseInt(hhmmss.substring(0, 2));
        int minute = Integer.parseInt(hhmmss.substring(2, 4));
        int second = Integer.parseInt(hhmmss.substring(4, 6));

        final Calendar calendar = TimeUtils.getUTCCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        long timeInMillis = calendar.getTime().getTime();
        return (int) (timeInMillis / 1000);
    }

    static String[] tokenize(String line) {
        line = line.replaceAll(" +", " "); // some fields are separated by two or more blanks (sigh) tb 2023-04-12
        return StringUtils.split(line, new char[]{' '}, true);
    }
}
