/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sven Dubbeld
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.svendubbeld.car.obd;

import java.util.HashMap;

public abstract class VIN {

    private static HashMap<String, String> getWMIMap() {
        HashMap<String, String> wmiMap = new HashMap<>();
        wmiMap.put("AFA", "Ford");
        wmiMap.put("AAV", "Volkswagen");
        wmiMap.put("JA3", "Mitsubishi");
        wmiMap.put("JA9", "Isuzu");
        wmiMap.put("JF9", "Subaru");
        wmiMap.put("JHM", "Honda");
        wmiMap.put("JHG", "Honda");
        wmiMap.put("JHL", "Honda");
        wmiMap.put("JK9", "Kawasaki");
        wmiMap.put("JM9", "Mazda");
        wmiMap.put("JN9", "Nissan");
        wmiMap.put("JS9", "Suzuki");
        wmiMap.put("JT9", "Toyota");
        wmiMap.put("KL9", "Daewoo");
        wmiMap.put("KM8", "Hyundai");
        wmiMap.put("KMH", "Hyundai");
        wmiMap.put("KNA", "Kia");
        wmiMap.put("KNB", "Kia");
        wmiMap.put("KNC", "Kia");
        wmiMap.put("KNM", "Renault");
        wmiMap.put("KPA", "Ssangyong");
        wmiMap.put("KPT", "Ssangyong");
        wmiMap.put("L56", "Renault");
        wmiMap.put("L5Y", "Merato");
        wmiMap.put("LDY", "Zhongtong");
        wmiMap.put("LGH", "Dong Feng");
        wmiMap.put("LKL", "Suzhou King Long");
        wmiMap.put("LSY", "Brilliance Zhonghua");
        wmiMap.put("LTV", "Toyota");
        wmiMap.put("LVS", "Ford");
        wmiMap.put("LVV", "Chery");
        wmiMap.put("LZM", "MAN");
        wmiMap.put("LZE", "Isuzu Guangzhou");
        wmiMap.put("LZG", "Shaanxi");
        wmiMap.put("LZY", "Yutong Zhengzhou");
        wmiMap.put("MA1", "Mahindra");
        wmiMap.put("MA3", "Suzuki");
        wmiMap.put("MA7", "Honda");
        wmiMap.put("MAL", "Hyundai");
        wmiMap.put("MC2", "Volvo");
        wmiMap.put("MHR", "Honda");
        wmiMap.put("MNB", "Ford");
        wmiMap.put("MNT", "Nissan");
        wmiMap.put("MMB", "Mitsubishi");
        wmiMap.put("MMM", "Chevrolet");
        wmiMap.put("MMT", "Mitsubishi");
        wmiMap.put("MM8", "Mazda");
        wmiMap.put("MPA", "Isuzu");
        wmiMap.put("MP1", "Isuzu");
        wmiMap.put("MRH", "Honda");
        wmiMap.put("MR0", "Toyota");
        wmiMap.put("NLE", "Mercedes-Benz");
        wmiMap.put("NM0", "Ford");
        wmiMap.put("NM4", "Tofas");
        wmiMap.put("NMT", "Toyota");
        wmiMap.put("PE1", "Ford");
        wmiMap.put("PE3", "Mazda");
        wmiMap.put("PL1", "Proton");
        wmiMap.put("SAL", "Land Rover");
        wmiMap.put("SAJ", "Jaguar");
        wmiMap.put("SAR", "Rover");
        wmiMap.put("SCA", "Rolls Royce");
        wmiMap.put("SCC", "Lotus");
        wmiMap.put("SCE", "DeLorean");
        wmiMap.put("SCF", "Aston");
        wmiMap.put("SDB", "Peugeot");
        wmiMap.put("SFD", "Alexander Dennis");
        wmiMap.put("SHS", "Honda");
        wmiMap.put("SJN", "Nissan");
        wmiMap.put("SU9", "Solaris");
        wmiMap.put("TK9", "SOR");
        wmiMap.put("TDM", "QUANTYA");
        wmiMap.put("TMB", "Škoda");
        wmiMap.put("TMK", "Karosa");
        wmiMap.put("TMP", "Škoda");
        wmiMap.put("TMT", "Tatra");
        wmiMap.put("TM9", "Škoda");
        wmiMap.put("TN9", "Karosa");
        wmiMap.put("TRA", "Ikarus");
        wmiMap.put("TRU", "Audi");
        wmiMap.put("TSE", "Ikarus");
        wmiMap.put("TSM", "Suzuki");
        wmiMap.put("UU1", "Dacia");
        wmiMap.put("VF1", "Renault");
        wmiMap.put("VF3", "Peugeot");
        wmiMap.put("VF6", "Renault");
        wmiMap.put("VF7", "Citroën");
        wmiMap.put("VF8", "Matra");
        wmiMap.put("VLU", "Scania");
        wmiMap.put("VNE", "Irisbus");
        wmiMap.put("VSE", "Suzuki");
        wmiMap.put("VSK", "Nissan");
        wmiMap.put("VSS", "SEAT");
        wmiMap.put("VSX", "Opel");
        wmiMap.put("VS6", "Ford");
        wmiMap.put("VS9", "Carrocerias Ayats");
        wmiMap.put("VWV", "Volkswagen");
        wmiMap.put("VX1", "Zastava");
        wmiMap.put("WAG", "Neoplan");
        wmiMap.put("WAU", "Audi");
        wmiMap.put("WBA", "BMW");
        wmiMap.put("WBS", "BMW M");
        wmiMap.put("WDB", "Mercedes-Benz");
        wmiMap.put("WDC", "DaimlerChrysler");
        wmiMap.put("WDD", "McLaren");
        wmiMap.put("WEB", "Evobus");
        wmiMap.put("WF0", "Ford");
        wmiMap.put("WMA", "MAN");
        wmiMap.put("WMW", "MINI");
        wmiMap.put("WP0", "Porsche");
        wmiMap.put("W0L", "Opel");
        wmiMap.put("WVW", "Volkswagen");
        wmiMap.put("WV1", "Volkswagen");
        wmiMap.put("WV2", "Volkswagen");
        wmiMap.put("XL9", "Spyker");
        wmiMap.put("XMC", "Mitsubishi");
        wmiMap.put("XTA", "Lada");
        wmiMap.put("YK1", "Saab");
        wmiMap.put("YS2", "Scania");
        wmiMap.put("YS3", "Saab");
        wmiMap.put("YS4", "Scania");
        wmiMap.put("YV1", "Volvo");
        wmiMap.put("YV2", "Volvo");
        wmiMap.put("YV3", "Volvo");
        wmiMap.put("YV4", "Volvo");
        wmiMap.put("ZAM", "Maserati");
        wmiMap.put("ZAP", "Piaggio");
        wmiMap.put("ZAR", "Alfa Romeo");
        wmiMap.put("ZCG", "Cagiva");
        wmiMap.put("ZDM", "Ducati");
        wmiMap.put("ZDF", "Ferrari");
        wmiMap.put("ZD4", "Aprilia");
        wmiMap.put("ZFA", "Fiat");
        wmiMap.put("ZFC", "Fiat");
        wmiMap.put("ZFF", "Ferrari");
        wmiMap.put("ZHW", "Lamborghini");
        wmiMap.put("ZLA", "Lancia");
        wmiMap.put("ZOM", "OM");
        wmiMap.put("1C3", "Chrysler");
        wmiMap.put("1C6", "Chrysler");
        wmiMap.put("1D3", "Dodge");
        wmiMap.put("1FA", "Ford");
        wmiMap.put("1FB", "Ford");
        wmiMap.put("1FC", "Ford");
        wmiMap.put("1FD", "Ford");
        wmiMap.put("1FM", "Ford");
        wmiMap.put("1FT", "Ford");
        wmiMap.put("1FU", "Freightliner");
        wmiMap.put("1FV", "Freightliner");
        wmiMap.put("1F9", "FWD");
        wmiMap.put("1G9", "GM");
        wmiMap.put("1GC", "Chevrolet");
        wmiMap.put("1GT", "GMC");
        wmiMap.put("1G1", "Chevrolet");
        wmiMap.put("1G2", "Pontiac");
        wmiMap.put("1G3", "Oldsmobile");
        wmiMap.put("1G4", "Buick");
        wmiMap.put("1G6", "Cadillac");
        wmiMap.put("1GM", "Pontiac");
        wmiMap.put("1G8", "Saturn");
        wmiMap.put("1H9", "Honda");
        wmiMap.put("1HD", "Harley-Davidson");
        wmiMap.put("1J4", "Jeep");
        wmiMap.put("1L9", "Lincoln");
        wmiMap.put("1ME", "Mercury");
        wmiMap.put("1M1", "Mack");
        wmiMap.put("1M2", "Mack");
        wmiMap.put("1M3", "Mack");
        wmiMap.put("1M4", "Mack");
        wmiMap.put("1M9", "Mynatt");
        wmiMap.put("1N9", "Nissan");
        wmiMap.put("1NX", "NUMMI");
        wmiMap.put("1P3", "Plymouth");
        wmiMap.put("1R9", "Roadrunner Hay Squeeze");
        wmiMap.put("1VW", "Volkswagen");
        wmiMap.put("1XK", "Kenworth");
        wmiMap.put("1XP", "Peterbilt");
        wmiMap.put("1YV", "Mazda");
        wmiMap.put("2C3", "CHrysler");
        wmiMap.put("2CN", "CAMI");
        wmiMap.put("2D3", "Dodge");
        wmiMap.put("2FA", "Ford");
        wmiMap.put("2FB", "Ford");
        wmiMap.put("2FC", "Ford");
        wmiMap.put("2FM", "Ford");
        wmiMap.put("2FT", "Ford");
        wmiMap.put("2FU", "Freightliner");
        wmiMap.put("2FV", "Freightliner");
        wmiMap.put("2FZ", "Sterling");
        wmiMap.put("2G9", "GM");
        wmiMap.put("2G1", "Chevrolet");
        wmiMap.put("2G2", "Pontiac");
        wmiMap.put("2G3", "Oldsmobile");
        wmiMap.put("2G4", "Buick");
        wmiMap.put("2HG", "Honda");
        wmiMap.put("2HK", "Honda");
        wmiMap.put("2HM", "Hyundai");
        wmiMap.put("2M9", "Mercury");
        wmiMap.put("2NV", "Nova");
        wmiMap.put("2P3", "Plymouth");
        wmiMap.put("2T9", "Toyota");
        wmiMap.put("2V4", "Volkswagen");
        wmiMap.put("2WK", "Western Star");
        wmiMap.put("2WL", "Western Star");
        wmiMap.put("2WM", "Western Star");
        wmiMap.put("3D3", "Dodge");
        wmiMap.put("3FA", "Ford");
        wmiMap.put("3FE", "Ford");
        wmiMap.put("3G9", "GM");
        wmiMap.put("3H9", "Honda");
        wmiMap.put("3N9", "Nissan");
        wmiMap.put("3P3", "Plymouth");
        wmiMap.put("3VW", "Volkswagen");
        wmiMap.put("4F9", "Mazda");
        wmiMap.put("4M9", "Mercury");
        wmiMap.put("4RK", "Nova");
        wmiMap.put("4S9", "Subaru");
        wmiMap.put("4T9", "Toyota");
        wmiMap.put("4US", "BMW");
        wmiMap.put("4UZ", "Frt-Thomas");
        wmiMap.put("4V1", "Volvo");
        wmiMap.put("4V2", "Volvo");
        wmiMap.put("4V3", "Volvo");
        wmiMap.put("4V4", "Volvo");
        wmiMap.put("4V5", "Volvo");
        wmiMap.put("4V6", "Volvo");
        wmiMap.put("4VL", "Volvo");
        wmiMap.put("4VM", "Volvo");
        wmiMap.put("4VZ", "Volvo");
        wmiMap.put("5F9", "Honda");
        wmiMap.put("5L9", "Lincoln");
        wmiMap.put("5N1", "Nissan");
        wmiMap.put("5NP", "Hyundai");
        wmiMap.put("5T9", "Toyota");
        wmiMap.put("6AB", "MAN");
        wmiMap.put("6F4", "Nissan");
        wmiMap.put("6F5", "Kenworth");
        wmiMap.put("6FP", "Ford");
        wmiMap.put("6G1", "GM");
        wmiMap.put("6G2", "Pontiac");
        wmiMap.put("6H8", "GM");
        wmiMap.put("6MM", "Mitsubishi");
        wmiMap.put("6T1", "Toyota");
        wmiMap.put("6U9", "Custom");
        wmiMap.put("8AG", "Chevrolet");
        wmiMap.put("8GG", "Chevrolet");
        wmiMap.put("8AP", "Fiat");
        wmiMap.put("8AF", "Ford");
        wmiMap.put("8AD", "Peugeot");
        wmiMap.put("8GD", "Peugeot");
        wmiMap.put("8A1", "Renault");
        wmiMap.put("8AK", "Suzuki");
        wmiMap.put("8AJ", "Toyota");
        wmiMap.put("8AW", "Volkswagen");
        wmiMap.put("93U", "Audi");
        wmiMap.put("9BG", "Chevrolet");
        wmiMap.put("935", "Citroën");
        wmiMap.put("9BD", "Fiat");
        wmiMap.put("9BF", "Ford");
        wmiMap.put("93H", "Honda");
        wmiMap.put("9BM", "Mercedes-Benz");
        wmiMap.put("936", "Peugeot");
        wmiMap.put("93Y", "Renualt");
        wmiMap.put("9BS", "Scania");
        wmiMap.put("93R", "Toyota");
        wmiMap.put("9BW", "Volkswagen");
        wmiMap.put("9FB", "Renault");

        return wmiMap;
    }

    private static String getWMI(String wmi) {
        String make = getWMIMap().get(wmi);
        return make != null ? make : "Unknown";
    }

    private static HashMap<String, String> getYearMap() {
        HashMap<String, String> yearMap = new HashMap<>();
        yearMap.put("M", "1991");
        yearMap.put("N", "1992");
        yearMap.put("P", "1993");
        yearMap.put("R", "1994");
        yearMap.put("S", "1995");
        yearMap.put("T", "1996");
        yearMap.put("V", "1997");
        yearMap.put("W", "1998");
        yearMap.put("X", "1999");
        yearMap.put("Y", "2000");
        yearMap.put("1", "2001");
        yearMap.put("2", "2002");
        yearMap.put("3", "2003");
        yearMap.put("4", "2004");
        yearMap.put("5", "2005");
        yearMap.put("6", "2006");
        yearMap.put("7", "2007");
        yearMap.put("8", "2008");
        yearMap.put("9", "2009");
        yearMap.put("A", "2010");
        yearMap.put("B", "2011");
        yearMap.put("C", "2012");
        yearMap.put("D", "2013");
        yearMap.put("E", "2014");
        yearMap.put("F", "2015");
        yearMap.put("G", "2016");
        yearMap.put("H", "2017");
        yearMap.put("J", "2018");
        yearMap.put("K", "2019");
        yearMap.put("L", "2020");

        return yearMap;
    }

    private static String getYear(String vis) {
        String year = getYearMap().get(vis.substring(0, 1));
        return year != null ? year : "Unknown";
    }

    public static String resolveVin(String vin) {
        String wmi = vin.substring(0, 3);
        String vds = vin.substring(3, 9);
        String vis = vin.substring(9, 17);

        return getWMI(wmi) + " (" + getYear(vis) + ")";
    }

}
