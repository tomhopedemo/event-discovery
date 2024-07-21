package com.events;

import java.util.*;

import static com.events.Util.list;

class London {
    static Util.Table londonAreaConfig;
    static final Map<String, List<String>> SUBPARTS = Util.map();
    final static String LONDON_MAP = ",,,,,,Highgate,,,Harringay,,,,,\n" + ",,,Hampstead,,,Archway,Holloway,Finsbury Park,Stoke Newington,Hackney Downs,,,,\n" + ",,West Hampstead,Finchley Road,,,Kentish Town,Caledonian Road,Highbury Corner,Canonbury,Dalston,Kingsland,Homerton,Hackney Wick,Stratford\n" + ",,,Swiss Cottage,,,Camden,Barnsbury,Upper Street,Rosemary Gardens,Haggerston,London Fields,,,\n" + ",,North Kensington,St John's Wood,Regent's Park,Regent's Park,Euston,Kings Cross,Angel,Hoxton,Hoxton,Broadway Market,,,\n" + ",White City,Notting Hill,Paddington,Marylebone,Marylebone,Fitzrovia,Bloomsbury,Clerkenwell,Old Street,Shoreditch,Cambridge Heath,Bethnal Green,Bow,\n" + ",Shepherd's Bush,Holland Park,Hyde Park,Mayfair,Soho,Holborn,Chancery Lane,Farringdon,Barbican,Liverpool Street,Spitalfields,,,\n" + ",,Kensington,Hyde Park,Mayfair,Piccadilly,Covent Garden,Aldwych,St Pauls,Bank,Aldgate,Whitechapel,Limehouse,,Poplar\n" + ",,Kensington,Knightsbridge,Belgravia,Westminster,Waterloo,Southbank,Bankside,London Bridge,Tower Bridge,Wapping,Wapping,Canary Wharf,\n" + ",Turnham Green,Hammersmith,Earls Court,Chelsea,Pimlico,Lambeth,Southwark,Borough,Bermondsey,Bermondsey,RIVER,RIVER,RIVER,\n" + ",,RIVER,Fulham,RIVER,RIVER,Vauxhall,Kennington,Elephant,,,,Canada Water,Greenwich,\n" + ",,Barnes,RIVER,Battersea,Nine Elms,Stockwell,Camberwell,Peckham,,,Deptford,New Cross,Rotherhithe,\n" + ",,Putney,,Clapham,Clapham,Brixton,,,,,,,,\n" + ",,,,,Balham,Streatham,,,,,,,,\n" + ",,,,,,,,,Nunhead,,,,,";

    static {
        SUBPARTS.put("north london", list("finsbury park"));
        SUBPARTS.put("south london", list("elephant"));
        SUBPARTS.put("west london", list("hammersmith"));
        SUBPARTS.put("east london", list("Whitechapel"));
        SUBPARTS.put("hackney", list("hoxton", "haggerston", "shoreditch"));
        SUBPARTS.put("aldwych", list("fleet street"));
        SUBPARTS.put("bank", list("leadenhall market"));
        SUBPARTS.put("the city", list("liverpool street", "bank", "aldgate"));
        SUBPARTS.put("victoria", list("belgravia", "westminster", "pimlico"));
        SUBPARTS.put("oxford street", list("marylebone", "fitzrovia", "soho"));
        SUBPARTS.put("trafalgar square", list("covent garden", "piccadilly", "westminster"));
        SUBPARTS.put("harringay", list("manor house"));
        SUBPARTS.put("tuffnell park", list("archway"));
        SUBPARTS.put("cannon street", list("bank"));
        SUBPARTS.put("monument", list("bank"));
        SUBPARTS.put("ladbroke grove", list("notting hill", "north kensington"));
        SUBPARTS.put("guildhall", list("st pauls", "liverpool street", "bank"));
        SUBPARTS.put("temple", list("aldwych"));
        SUBPARTS.put("leicester square", list("soho", "piccadilly", "covent garden"));
        SUBPARTS.put("hyde park corner", list("hyde park", "mayfair", "belgravia"));
        SUBPARTS.put("embankment", list("covent garden"));
        SUBPARTS.put("strand", list("covent garden", "aldwych"));
        SUBPARTS.put("fleet street", list("aldwych", "st pauls"));
        SUBPARTS.put("sloane square", list("victoria", "belgravia"));
        SUBPARTS.put("manor house", list("harringay", "stoke newington"));
        SUBPARTS.put("oval", list("kennington"));
        List<List<String>> table = list();
        for (String line : Util.splitList(London.LONDON_MAP, "\n")) {
            table.add(Util.splitList(line, ","));
        }
        londonAreaConfig = new Util.Table(table);
    }

    static final List<String> FAROUT = list("wembley", "alexandra park", "meridian water", "seven sisters", "southgate", "twickenham", "richmond", "wimbledon");
    static final List<String> ODD = list("aldwych", "leadenhall market", "fenchurch street", "rosemary gardens", "bow", "st pauls");
    static List<String> areasRestrict = list("camden", "farringdon", "chelsea", "mayfair", "piccadilly", "north london", "south london", "liverpool street", "west london", "east london", "regent's park", "oxford street", "victoria", "st pauls", "hammersmith", "kensington", "wembley", "covent garden", "kings cross", "stoke newington", "stratford", "marylebone", "soho", "shoreditch", "holborn", "leicester square", "hackney", "greenwich", "london bridge", "waterloo", "wimbledon", "clapham");

    static List<String> areasDisplay() {
        ArrayList<String> to_return = new ArrayList<>(London.areasRestrict);
        to_return.removeAll(FAROUT);
        to_return.removeAll(ODD);
        return to_return;
    }

    static Util.MultiList<Double, String> areasOrderedByProximity(String area) {
        List<String> areas;
        if (SUBPARTS.containsKey(area)) {
            areas = SUBPARTS.get(area);
            areas.add(area);
        } else {
            areas = list(area);
        }
        Util.MultiList<Integer, Integer> areaCoordinates = new Util.MultiList<>();
        for (String areaLocal : areas) {
            Util.addAll(areaCoordinates, londonAreaConfig.getIgnoreCase(areaLocal));
        }
        if (Util.empty(areaCoordinates)) return null;
        Util.MultiList<Double, String> distance_area = new Util.MultiList<>();
        distance_area.add(new Util.Multi<>(0d, area));
        for (int i = 0; i < londonAreaConfig.underlying.size(); i++) {
            List<String> row = londonAreaConfig.underlying.get(i);
            for (int j = 0; j < row.size(); j++) {
                String element = row.get(j);
                if (Util.empty(element)) continue;
                if (area.equalsIgnoreCase(element)) continue;
                Double distance_min = null;
                for (Util.Multi<Integer, Integer> coordinate : areaCoordinates.underlying) {
                    double distance = distanceAbsolute(coordinate, new Util.Multi<>(i, j));
                    if (distance_min == null || distance < distance_min) {
                        distance_min = distance;
                    }
                }
                distance_area.add(new Util.Multi<>(distance_min, element));
            }
        }
        Collections.sort(distance_area.underlying, Comparator.comparing(o -> o.a));
        return distance_area;
    }

    static double distanceAbsolute(Util.Multi<Integer, Integer> coordinate_a, Util.Multi<Integer, Integer> coordinate_b) {
        return Math.sqrt(Math.pow(coordinate_a.a - coordinate_b.a, 2) + Math.pow(coordinate_a.b - coordinate_b.b, 2));
    }
}