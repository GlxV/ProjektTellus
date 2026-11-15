package com.projekttellus;

import com.projekttellus.domain.*;
import com.projekttellus.events.*;
import com.projekttellus.simulation.*;
import com.projekttellus.ui.AtmosphereOptics;
import com.projekttellus.persistence.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/** Executable, offline experimental audit. No JUnit, GUI, Maven or personal database. */
public final class ExperimentalAudit {
    private static final double[] YEARS = {0, 1, 10, 100, 1000};
    private static final double[] INTENSITIES = {0, .01, .25, .5, .75, 1};
    private static final EventoCreator[] EVENTS = {new AsteroideCreator(), new SupervulcaoCreator(),
            new GuerraNuclearCreator(), new InvasaoAlienigenaCreator(), new RestauracaoEcologicaCreator()};
    private static final String[] FIELDS = ("o2,n2,co2,he,pressure,temperature,aerosol,radiation,vegetation,water,ice,population,years,"
            + "land,vegetated_area,water_reserve,gas_sum,po2,pco2,target_temperature,H,B,C,K,"
            + "molecular_mass,scattering_weight,column,vertical_scale,rayleigh,particles,thickness,transmission,halo_opacity,"
            + "reference_H,reference_B,reference_C,reference_K").split(",");
    private final Path out;
    private final PrintWriter states, checks, factors, rejects;
    private long experiments, snapshots, checkCount, failures, rejectionCount, factorRows;
    private final Map<String, Long> categories = new LinkedHashMap<>();
    private final Map<String, long[]> ruleCounts = new TreeMap<>();
    private final Map<String, Double> ruleMaxError = new TreeMap<>();
    private final EstadoPlanetario base = new TerraBuilder().build().estado();
    private ExperimentalAudit(Path out) throws IOException {
        this.out = out;
        Files.createDirectories(out);
        states = writer("snapshots.csv"); checks = writer("checks.csv.gz");
        factors = writer("factors.csv.gz"); rejects = writer("rejections.csv");
        var header = new ArrayList<String>(List.of("experiment", "family", "case", "operation", "intensity", "horizon_years"));
        for (String prefix : List.of("before_", "after_")) for (String field : FIELDS) header.add(prefix + field);
        csv(states, header.toArray());
        csv(checks, "experiment", "case", "check", "pass", "actual", "expected", "tolerance");
        csv(factors, "experiment", "case", "horizon_years", "side", "indicator", "factor_index", "name", "value", "unit", "adequacy");
        csv(rejects, "experiment", "case", "expected_exception", "actual_exception", "message", "pass");
    }
    private PrintWriter writer(String name) throws IOException {
        OutputStream stream=Files.newOutputStream(out.resolve(name));
        // JDK GZIPOutputStream writes an MTIME=0 header: compressed CSVs have reproducible gzip metadata.
        if(name.endsWith(".gz"))stream=new GZIPOutputStream(stream);
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream,StandardCharsets.UTF_8)));
    }
    private static void csv(PrintWriter w, Object... values) {
        for (int i=0;i<values.length;i++) {
            if(i>0) w.print(',');
            String s = String.valueOf(values[i]);
            if(s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) w.print("\"" + s.replace("\"", "\"\"") + "\"");
            else w.print(s);
        }
        w.println();
    }
    private long experiment(String family) { categories.merge(family,1L,Long::sum); return ++experiments; }
    private void check(long id, String name, String rule, boolean pass, Object actual, Object expected, double tolerance) {
        checkCount++; if(!pass) failures++;
        var counts=ruleCounts.computeIfAbsent(rule,k->new long[2]);counts[0]++;if(!pass)counts[1]++;
        if(actual instanceof Number x && expected instanceof Number y && Double.isFinite(x.doubleValue()) && Double.isFinite(y.doubleValue()))
            ruleMaxError.merge(rule,Math.abs(x.doubleValue()-y.doubleValue()),Math::max);
        csv(checks,id,name,rule,pass,actual,expected,tolerance);
    }
    private void near(long id, String name, String rule, double actual, double expected, double tolerance) {
        check(id,name,rule,Double.isFinite(actual) && Math.abs(actual-expected)<=tolerance,actual,expected,tolerance);
    }
    private void eq(long id, String name, String rule, Object actual, Object expected) {
        check(id,name,rule,Objects.equals(actual,expected),actual,expected,0);
    }
    private static double clamp(double x,double low,double high) {return Math.max(low,Math.min(high,x));}
    private static double smooth(double x) {double u=clamp(x,0,1);return u*u*(3-2*u);}
    private static double range(double x,double a,double b,double c,double d) {return smooth((x-a)/(b-a))*smooth((d-x)/(d-c));}
    /** Independent evaluation of the documented algebra; not used to evolve production states. */
    private static double[] reference(EstadoPlanetario e) {
        var a=e.atmosfera(); var c=e.clima(); var w=e.hidrosfera();
        double target=clamp(15+4*Math.log((a.pressao()*a.co2()/100+.00001)/.00041)
                +3*Math.log(Math.max(.05,a.pressao()))-.35*c.aerossol()-.18*(w.gelo()-3),-100,120);
        double H=100*range(a.pressao()*a.oxigenio()/100,.06,.16,.30,.60)*range(a.pressao(),.15,.7,1.5,6)
                /(1+Math.pow(Math.max(0,a.pressao()*a.co2()/100-.0004)/.003,2))
                *range(c.temperatura(),-40,5,25,65)*smooth(w.agua()/15)*(1-c.radiacao()/100)*Math.exp(-2*c.aerossol()/100);
        double B=100*range(c.temperatura(),-60,0,45,130)*smooth(w.agua()/10)*smooth(a.pressao()/.15)
                /(1+Math.max(0,a.pressao()-5)/10)*Math.sqrt(1-c.radiacao()/100)*Math.exp(-1.2*c.aerossol()/100)
                /(1+Math.abs(target-c.temperatura())/45);
        double L=Math.max(0,100-w.agua()-w.gelo())/100, Q=Math.sqrt(.25+.75*e.biosfera().vegetacao()/100), W=smooth(w.agua()/15);
        long K=Math.round(clamp(16e9*(L/.26)*Q*W*(H/100)*Math.sqrt(B/100),0,100e9));
        double D=e.civilizacao().populacao()==0?1:Math.min(1,(double)K/e.civilizacao().populacao());
        double C=100*(H/100)*smooth(L/.10)*W*Q*Math.sqrt(B/100)*D;
        return new double[]{H,B,C,K,target};
    }
    private static double[] values(EstadoPlanetario e) {
        var a=e.atmosfera();var c=e.clima();var h=e.hidrosfera();var i=ModeloPlanetario.calcular(e);var o=AtmosphereOptics.from(e);var r=reference(e);
        double mass=(32*a.oxigenio()+28*a.nitrogenio()+44*a.co2()+4*a.helio())/100;
        double weight=(.85*a.oxigenio()+a.nitrogenio()+2.5*a.co2()+.014*a.helio())/100;
        return new double[]{a.oxigenio(),a.nitrogenio(),a.co2(),a.helio(),a.pressao(),c.temperatura(),c.aerossol(),c.radiacao(),
                e.biosfera().vegetacao(),h.agua(),h.gelo(),e.civilizacao().populacao(),e.anos(),e.superficieContinental(),e.superficieVegetada(),
                h.agua()+h.gelo(),a.oxigenio()+a.nitrogenio()+a.co2()+a.helio(),a.pressaoParcialOxigenio(),a.pressaoParcialCo2(),ModeloPlanetario.temperaturaAlvo(e),
                i.humana(),i.biologica(),i.sustentabilidade(),i.capacidadeSuporte(),mass,weight,a.pressao()*28.8/mass,
                (c.temperatura()+273.15)/288.15*28.8/mass,o.rayleigh(),o.particles(),o.thickness(),o.transmission(),o.haloOpacity(),r[0],r[1],r[2],r[3]};
    }
    private void snapshot(long id,String family,String name,String operation,double intensity,double horizon,EstadoPlanetario before,EstadoPlanetario after) {
        snapshots++;
        List<Object> row=new ArrayList<>(List.of(id,family,name,operation,intensity,horizon));
        for(double x:values(before))row.add(x);for(double x:values(after))row.add(x);csv(states,row.toArray());
        validate(id,name+"@"+horizon,after);
        writeFactors(id,name,horizon,"before",before);writeFactors(id,name,horizon,"after",after);
    }
    private void writeFactors(long id,String name,double horizon,String side,EstadoPlanetario s) {
        var i=ModeloPlanetario.calcular(s);
        var lists=List.of(i.fatoresHumanos(),i.fatoresBiologicos(),i.fatoresCivilizacao());
        for(int k=0;k<3;k++)for(int j=0;j<lists.get(k).size();j++) {
            var f=lists.get(k).get(j);factorRows++;
            csv(factors,id,name,horizon,side,List.of("H","B","C").get(k),j,f.nome(),f.valor(),f.unidade(),f.adequacao());
        }
    }
    private void validate(long id,String name,EstadoPlanetario s) {
        double[] v=values(s);var i=ModeloPlanetario.calcular(s);var r=reference(s);var o=AtmosphereOptics.from(s);
        for(int j=0;j<v.length;j++)check(id,name,"finite_"+FIELDS[j],Double.isFinite(v[j]),v[j],"finite",0);
        near(id,name,"gas_sum",v[16],100,1e-9);
        check(id,name,"water_area_bound",v[15]>=0&&v[15]<=100+1e-9,v[15],"[0,100]",1e-9);
        check(id,name,"domain_bounds",v[4]>=0&&v[4]<=20&&v[5]>=-150&&v[5]<=200&&v[6]>=0&&v[6]<=100&&v[7]>=0&&v[7]<=100
                &&v[8]>=0&&v[8]<=100&&v[9]>=0&&v[10]>=0&&v[11]>=0&&v[11]<=100e9&&v[12]>=0&&v[12]<=1e9,"state","valid",0);
        near(id,name,"reference_H",i.humana(),r[0],1e-9);near(id,name,"reference_B",i.biologica(),r[1],1e-9);
        near(id,name,"reference_C",i.sustentabilidade(),r[2],1e-9);near(id,name,"reference_K",i.capacidadeSuporte(),r[3],1);
        near(id,name,"reference_target",v[19],r[4],1e-12);
        double expectedR=s.atmosfera().pressao()==0?0:.12*v[26]*v[25];
        double expectedP=s.atmosfera().pressao()==0?0:1.8*s.clima().aerossol()/100*s.atmosfera().pressao()/(s.atmosfera().pressao()+.05);
        double expectedD=s.atmosfera().pressao()==0?0:clamp(.022+.018*Math.log1p(s.atmosfera().pressao())+.028*Math.log1p(v[27]),.02,.16);
        near(id,name,"reference_rayleigh",o.rayleigh(),expectedR,1e-12);near(id,name,"reference_particles",o.particles(),expectedP,1e-12);
        near(id,name,"reference_thickness",o.thickness(),expectedD,1e-12);near(id,name,"transmission",o.transmission(),Math.exp(-expectedR-expectedP),1e-12);
        check(id,name,"bounded_scores",i.humana()>=0&&i.humana()<=100&&i.biologica()>=0&&i.biologica()<=100&&i.sustentabilidade()>=0&&i.sustentabilidade()<=100,"HBC","[0,100]",0);
        check(id,name,"bounded_optics",o.haloOpacity()>=0&&o.haloOpacity()<=1&&o.transmission()>=0&&o.transmission()<=1,"optics","[0,1]",0);
        eq(id,name,"calculation_determinism",i,ModeloPlanetario.calcular(s));eq(id,name,"optics_determinism",o,AtmosphereOptics.from(s));
        if(s.superficieContinental()<1e-9){near(id,name,"no_land_K",i.capacidadeSuporte(),0,0);near(id,name,"no_land_C",i.sustentabilidade(),0,0);}
        int k=0;for(var fs:List.of(i.fatoresHumanos(),i.fatoresBiologicos(),i.fatoresCivilizacao())) {
            double product=100;for(var f:fs)product*=f.adequacao();
            near(id,name,"factor_product_"+k,product,new double[]{i.humana(),i.biologica(),i.sustentabilidade()}[k++],1e-9);
        }
    }
    private static EstadoPlanetario config(double o,double n,double c,double he,double p,double t,double a,double r,double water,double ice,double veg,long pop) {
        return new EstadoPlanetario(new Atmosfera(o,n,c,he,p),new Clima(t,a,r),new Biosfera(veg),new Hidrosfera(water,ice),new Civilizacao(pop),0);
    }
    private EstadoPlanetario modify(Atmosfera a,Clima c,Hidrosfera h,Biosfera b,Civilizacao p) {
        return new EstadoPlanetario(a==null?base.atmosfera():a,c==null?base.clima():c,b==null?base.biosfera():b,h==null?base.hidrosfera():h,p==null?base.civilizacao():p,0);
    }
    private void single(String family,String name,EstadoPlanetario s) {long id=experiment(family);snapshot(id,family,name,"calculate",0,0,base,s);}
    private void trajectory(String family,String name,EstadoPlanetario initial) {
        long id=experiment(family);
        for(double y:YEARS) {
            var end=y==0?initial:ModeloPlanetario.avancar(initial,y);
            snapshot(id,family,name,"evolve",0,y,initial,end);
            if(y>0) evolutionChecks(id,name,initial,end,y);
        }
    }
    private void evolutionChecks(long id,String name,EstadoPlanetario before,EstadoPlanetario after,double y) {
        eq(id,name+"@"+y,"evolution_determinism",after,ModeloPlanetario.avancar(before,y));
        eq(id,name+"@"+y,"atmosphere_static",after.atmosfera(),before.atmosfera());
        near(id,name,"water_reserve_conservation",after.hidrosfera().agua()+after.hidrosfera().gelo(),before.hidrosfera().agua()+before.hidrosfera().gelo(),1e-9);
        near(id,name,"land_conservation",after.superficieContinental(),before.superficieContinental(),1e-9);
        near(id,name,"clock",after.anos(),before.anos()+y,1e-9);
        near(id,name,"aerosol_decay",after.clima().aerossol(),before.clima().aerossol()*Math.exp(-y/8),1e-10);
        near(id,name,"radiation_decay",after.clima().radiacao(),before.clima().radiacao()*Math.exp(-y/30),1e-10);
        if(before.civilizacao().populacao()==0)eq(id,name,"no_spontaneous_population",after.civilizacao().populacao(),0L);
    }
    private Map<String,EstadoPlanetario> scenarios() {
        Map<String,EstadoPlanetario> s=new LinkedHashMap<>();s.put("earth",base);
        s.put("ocean_legacy_he20_v93_pop10b",config(21,58.96,.04,20,1,15,0,0,100,0,93,10_000_000_000L));
        s.put("ocean",modify(null,null,new Hidrosfera(100,0),null,null));
        s.put("all_ice",modify(null,null,new Hidrosfera(0,100),null,null));
        s.put("dry",modify(null,null,new Hidrosfera(0,0),null,null));
        s.put("vacuum",modify(new Atmosfera(21,78.96,.04,0,0),null,null,null,null));
        s.put("thin",modify(new Atmosfera(21,78.96,.04,0,.01),null,null,null,null));
        s.put("pressure20",modify(new Atmosfera(21,78.96,.04,0,20),null,null,null,null));
        s.put("pure_o2",modify(new Atmosfera(100,0,0,0,1),null,null,null,null));
        s.put("pure_co2",modify(new Atmosfera(0,0,100,0,1),null,null,null,null));
        s.put("pure_he",modify(new Atmosfera(0,0,0,100,1),null,null,null,null));
        s.put("pure_n2",modify(new Atmosfera(0,100,0,0,1),null,null,null,null));
        s.put("anoxic",modify(new Atmosfera(0,99.96,.04,0,1),null,null,null,null));
        s.put("o2_2",modify(new Atmosfera(2,97.96,.04,0,1),null,null,null,null));
        s.put("co2_50",modify(new Atmosfera(21,29,50,0,1),null,null,null,null));
        s.put("he20",modify(new Atmosfera(21,58.96,.04,20,1),null,null,null,null));
        s.put("cold_min",modify(null,new Clima(-150,0,0),null,null,null));
        s.put("hot_max",modify(null,new Clima(200,0,0),null,null,null));
        s.put("radiation100",modify(null,new Clima(15,0,100),null,null,null));
        s.put("aerosol100",modify(null,new Clima(15,100,0),null,null,null));
        s.put("vegetation0",modify(null,null,null,new Biosfera(0),null));
        s.put("vegetation100",modify(null,null,null,new Biosfera(100),null));
        s.put("population0",modify(null,null,null,null,new Civilizacao(0)));
        s.put("population1",modify(null,null,null,null,new Civilizacao(1)));
        s.put("population100b",modify(null,null,null,null,new Civilizacao(100_000_000_000L)));
        s.put("land1",modify(null,null,new Hidrosfera(96,3),null,null));
        s.put("water1",modify(null,null,new Hidrosfera(1,3),null,null));
        s.put("max_degraded",config(0,0,100,0,20,200,100,100,0,100,0,100_000_000_000L));
        s.put("cold_vacuum_dry",config(0,0,0,100,0,-150,100,100,0,0,100,1));
        s.put("mixed_extreme",config(0,0,0,100,20,-150,100,100,100,0,100,100_000_000_000L));
        s.put("asteroid100",new AsteroideCreator().criar(1).aplicar(base));
        s.put("supervolcano100",new SupervulcaoCreator().criar(1).aplicar(base));
        s.put("nuclear100",new GuerraNuclearCreator().criar(1).aplicar(base));
        s.put("alien100",new InvasaoAlienigenaCreator().criar(1).aplicar(base));
        s.put("restoration_after_nuclear100",new RestauracaoEcologicaCreator().criar(1).aplicar(s.get("nuclear100")));
        s.put("frozen_cold_water5_ice69_v5",config(21,78.96,.04,0,1,-50,0,0,5,69,5,8_000_000_000L));
        return s;
    }
    private void sweeps() {
        // Integer-percent complete sweeps; N2 is the residual. Fixed nonzero gases limit admissible endpoints.
        for(int x=0;x<=99;x++) single("sweep_o2","O2="+x+";CO2=.04;He=0",modify(new Atmosfera(x,99.96-x,.04,0,1),null,null,null,null));
        single("sweep_o2","O2=99.96;CO2=.04;He=0",modify(new Atmosfera(99.96,0,.04,0,1),null,null,null,null));
        for(int x=0;x<=79;x++)single("sweep_co2","CO2="+x+";O2=21;He=0",modify(new Atmosfera(21,79-x,x,0,1),null,null,null,null));
        for(int x=0;x<=78;x++)heSweep(x);
        heSweep(78.96);
        for(int x=0;x<=100;x++) {
            single("sweep_o2_binary","O2="+x+";CO2=He=0",modify(new Atmosfera(x,100-x,0,0,1),null,null,null,null));
            single("sweep_co2_binary","CO2="+x+";O2=He=0",modify(new Atmosfera(0,100-x,x,0,1),null,null,null,null));
            single("sweep_he_binary","He="+x+";O2=CO2=0",modify(new Atmosfera(0,100-x,0,x,1),null,null,null,null));
        }
        for(int o=0;o<=100;o+=5)for(int c=0;c<=100-o;c+=5)
            single("sweep_ternary","O2="+o+";CO2="+c+";He="+(100-o-c),modify(new Atmosfera(o,0,c,100-o-c,1),null,null,null,null));
        for(double p:new double[]{0,1e-12,1e-9,.001,.01,.06,.15,.3,.5,.7,1,1.5,2,3,5,6,10,20})
            single("sweep_pressure","P="+p,modify(new Atmosfera(21,78.96,.04,0,p),null,null,null,null));
        for(double t:new double[]{-150,-100,-60,-40,-20,0,5,10,15,25,45,65,100,130,150,200})
            single("sweep_temperature","T="+t,modify(null,new Clima(t,0,0),null,null,null));
        for(int x=0;x<=100;x++) {
            single("sweep_water","water="+x+";ice=0",modify(null,null,new Hidrosfera(x,0),null,null));
            single("sweep_ice","ice="+x+";water=0",modify(null,null,new Hidrosfera(0,x),null,null));
            single("sweep_phase","ice="+x+";reserve=100",modify(null,null,new Hidrosfera(100-x,x),null,null));
            single("sweep_vegetation","V="+x,modify(null,null,null,new Biosfera(x),null));
        }
        for(long p:new long[]{0,1,10,1000,1_000_000,1_000_000_000L,8_000_000_000L,10_000_000_000L,16_000_000_000L,50_000_000_000L,100_000_000_000L})
            single("sweep_population","pop="+p,modify(null,null,null,null,new Civilizacao(p)));
        // Exact requested values not already present in the broad sweeps above; coverage map identifies both sets.
        single("request_pressure","P=0.1",modify(new Atmosfera(21,78.96,.04,0,.1),null,null,null,null));
        for(double t:new double[]{-50,30,50,80,120})single("request_temperature","T="+t,modify(null,new Clima(t,0,0),null,null,null));
        single("request_co2","CO2=0.1;O2=21;He=0",modify(new Atmosfera(21,78.9,.1,0,1),null,null,null,null));
        single("request_population","pop=100000000",modify(null,null,null,null,new Civilizacao(100_000_000L)));
    }
    private void heSweep(double he) {
        var s=modify(new Atmosfera(21,78.96-he,.04,he,1),null,null,null,null);
        long id=experiment("sweep_he_fixed");snapshot(id,"sweep_he_fixed","He="+he+";O2=21;CO2=.04","calculate",0,0,base,s);
        eq(id,"He="+he,"diluent_invariance",ModeloPlanetario.calcular(s),ModeloPlanetario.calcular(base));
    }
    private void events(Map<String,EstadoPlanetario> scenarios) {
        for(String name:List.of("earth","ocean_legacy_he20_v93_pop10b","cold_vacuum_dry","max_degraded","mixed_extreme","pure_o2","pure_co2","population0"))
            for(var creator:EVENTS)for(double intensity:INTENSITIES) {
                var initial=scenarios.get(name);var event=creator.criar(intensity);String label=name+"/"+creator.getClass().getSimpleName();
                long id=experiment("event");var immediate=event.aplicar(initial);
                eq(id,label,"event_determinism",immediate,event.aplicar(initial));
                near(id,label,"event_clock_static",immediate.anos(),initial.anos(),0);
                near(id,label,"event_water_reserve",immediate.hidrosfera().agua()+immediate.hidrosfera().gelo(),initial.hidrosfera().agua()+initial.hidrosfera().gelo(),1e-9);
                if(intensity==0)eq(id,label,"event_zero_identity",immediate,initial);
                eventOracle(id,label,creator,intensity,initial,immediate);
                for(double y:YEARS) {
                    var after=y==0?immediate:ModeloPlanetario.avancar(immediate,y);
                    snapshot(id,"event",label,creator.getClass().getSimpleName(),intensity,y,initial,after);
                    if(y>0)evolutionChecks(id,label,immediate,after,y);
                }
            }
    }
    private void eventOracle(long id,String label,EventoCreator creator,double i,EstadoPlanetario e,EstadoPlanetario actual) {
        double t=e.clima().temperatura(),a=e.clima().aerossol(),r=e.clima().radiacao(),v=e.biosfera().vegetacao(),w=e.hidrosfera().agua(),g=e.hidrosfera().gelo();
        double o=e.atmosfera().oxigenio(),n=e.atmosfera().nitrogenio(),c=e.atmosfera().co2(),he=e.atmosfera().helio(),p=e.atmosfera().pressao();
        long pop=e.civilizacao().populacao();
        if(creator instanceof RestauracaoEcologicaCreator) {
            t+=(15-t)*.45*i;a*=1-.7*i;r*=1-.65*i;v+=(100-v)*.45*i;double melt=g*.25*i;w+=melt;g-=melt;
            double transfer=Math.max(0,c-.04)*.6*i;c-=transfer;n+=transfer;
        } else {
            double[] k=creator instanceof AsteroideCreator?new double[]{28,85,0,.85,.75,.12,12}:
                    creator instanceof SupervulcaoCreator?new double[]{18,95,0,.55,.3,.4,8}:
                    creator instanceof GuerraNuclearCreator?new double[]{22,70,90,.8,.9,.08,10}:new double[]{-12,15,25,.6,.65,0,0};
            t=clamp(t-k[0]*i,-150,200);a=clamp(a+k[1]*i,0,100);r=clamp(r+k[2]*i,0,100);v*=1-k[3]*i;pop=Math.round(pop*(1-k[4]*i));
            double transfer=Math.min(n,k[5]*i);n-=transfer;c+=transfer;double freeze=Math.min(w,k[6]*i);w-=freeze;g+=freeze;
            if(creator instanceof InvasaoAlienigenaCreator){double extracted=o*.6*i;o-=extracted;he+=extracted;p*=1-.3*i;}
        }
        var expected=config(o,n,c,he,p,t,a,r,w,g,v,pop);
        double[] ev=values(expected),av=values(actual);
        for(int j=0;j<13;j++)near(id,label,"event_formula_"+FIELDS[j],av[j],ev[j],j==11?0:1e-9);
    }
    private void reject(String name,Class<? extends Throwable> type,Runnable action) {
        long id=experiment("rejection");rejectionCount++;Throwable thrown=null;
        try {action.run();}catch(Throwable ex){thrown=ex;}
        boolean pass=thrown!=null&&type.isInstance(thrown);
        csv(rejects,id,name,type.getName(),thrown==null?"NONE":thrown.getClass().getName(),thrown==null?"":thrown.getMessage(),pass);
        check(id,name,"rejection",pass,thrown==null?"accepted":thrown.getClass().getName(),type.getName(),0);
    }
    private void rejections() {
        for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,-1,101}) {
            reject("oxygen="+bad,IllegalArgumentException.class,()->new Atmosfera(bad,78.96,.04,0,1));
            reject("vegetation="+bad,IllegalArgumentException.class,()->new Biosfera(bad));
            reject("water="+bad,IllegalArgumentException.class,()->new Hidrosfera(bad,0));
            reject("aerosol="+bad,IllegalArgumentException.class,()->new Clima(15,bad,0));
            reject("radiation="+bad,IllegalArgumentException.class,()->new Clima(15,0,bad));
        }
        for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,-.01,20.01})reject("pressure="+bad,IllegalArgumentException.class,()->new Atmosfera(21,78.96,.04,0,bad));
        for(double bad:new double[]{Double.NaN,Double.NEGATIVE_INFINITY,-150.01,200.01})reject("temperature="+bad,IllegalArgumentException.class,()->new Clima(bad,0,0));
        for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,-1,0,.009,1000.01})reject("advance="+bad,IllegalArgumentException.class,()->ModeloPlanetario.avancar(base,bad));
        for(var creator:EVENTS)for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,-.01,1.01})reject(creator.getClass().getSimpleName()+"="+bad,IllegalArgumentException.class,()->creator.criar(bad));
        reject("population_negative",IllegalArgumentException.class,()->new Civilizacao(-1));
        reject("population_over_max",IllegalArgumentException.class,()->new Civilizacao(100_000_000_001L));
        reject("water_plus_ice",IllegalArgumentException.class,()->new Hidrosfera(71,30));
        reject("gas_sum_low",IllegalArgumentException.class,()->new Atmosfera(20,70,0,0,1));
        reject("gas_sum_high",IllegalArgumentException.class,()->new Atmosfera(21,79.1,0,0,1));
        reject("years_overflow",IllegalArgumentException.class,()->ModeloPlanetario.avancar(new EstadoPlanetario(base.atmosfera(),base.clima(),base.biosfera(),base.hidrosfera(),base.civilizacao(),1e9),.01));
    }
    private void probes() {
        for(double years:new double[]{.01,.1,.5,1.01,9.99}) {
            long id=experiment("fractional");var end=ModeloPlanetario.avancar(base,years);snapshot(id,"fractional","dt="+years,"evolve",0,years,base,end);evolutionChecks(id,"fractional",base,end,years);
        }
        long id=experiment("partition");var whole=ModeloPlanetario.avancar(base,100);var segmented=base;
        for(int j=0;j<100;j++)segmented=ModeloPlanetario.avancar(segmented,1);
        snapshot(id,"partition","100x1_vs_100","evolve",0,100,whole,segmented);eq(id,"partition","integer_partition_exact",segmented,whole);
        id=experiment("partition");var fraction=base;for(int j=0;j<100;j++)fraction=ModeloPlanetario.avancar(fraction,.1);
        snapshot(id,"partition","100x0.1_vs_10","integration_comparison",0,10,ModeloPlanetario.avancar(base,10),fraction);
        // This comparison deliberately records drift rather than requiring integration-step invariance.
        var random=new Random(20261115L);
        for(int j=0;j<250;j++) {
            double x=random.nextDouble(),y=random.nextDouble(),z=random.nextDouble(),q=random.nextDouble(),sum=x+y+z+q;
            double o=100*x/sum,c=100*z/sum,he=100*q/sum,n=100-o-c-he;
            double reserve=random.nextDouble()*100,ice=random.nextDouble()*reserve;
            var s=config(o,n,c,he,random.nextDouble()*20,-150+350*random.nextDouble(),100*random.nextDouble(),100*random.nextDouble(),reserve-ice,ice,100*random.nextDouble(),(long)(100e9*random.nextDouble()));
            trajectory("seeded_random","seed20261115_"+j,s);
        }
        // Near tolerance boundaries: acceptance + normalization, not invalid-input rejection.
        for(double noise:new double[]{-9e-7,-1e-13,0,1e-13,9e-7}) {
            var s=modify(new Atmosfera(21,78.96+noise,.04,0,1),null,null,null,null);single("normalization","gas_noise="+noise,s);
        }
        var s=modify(null,null,new Hidrosfera(71.0000009,29),null,null);single("normalization","hydro_noise=9e-7",s);
        for(String environment:List.of("earth","dry","ocean"))for(long pop:new long[]{0,1,2,3,4,5,10,41,42,43,100}) {
            var water=environment.equals("dry")?new Hidrosfera(0,0):environment.equals("ocean")?new Hidrosfera(100,0):base.hidrosfera();
            trajectory("rounding_probe",environment+"_pop"+pop,modify(null,null,water,null,new Civilizacao(pop)));
        }
        // Controlled local sensitivities, with explicit invariants rather than an assumed monotonic score for every parameter.
        sensitivity("vegetation_increase",modify(null,null,null,new Biosfera(66),null),new String[]{"H=","B=","K+","C+"});
        sensitivity("population_increase",modify(null,null,null,null,new Civilizacao(100_000_000_000L)),new String[]{"H=","B=","K=","C-"});
        sensitivity("temperature_16",modify(null,new Clima(16,0,0),null,null,null),new String[]{"H=","B-","rayleigh=","particles=","thickness+"});
        sensitivity("aerosol_1",modify(null,new Clima(15,1,0),null,null,null),new String[]{"H-","B-","rayleigh=","particles+","transmission-"});
        sensitivity("radiation_1",modify(null,new Clima(15,0,1),null,null,null),new String[]{"H-","B-","rayleigh=","particles=","transmission="});
        sensitivity("co2_1",modify(new Atmosfera(21,78,1,0,1),null,null,null,null),new String[]{"H-","B-","rayleigh+","transmission-"});
        sensitivity("oxygen_0",modify(new Atmosfera(0,99.96,.04,0,1),null,null,null,null),new String[]{"H-","B=","K-","C-"});
        sensitivity("water_72_same_reserve",modify(null,null,new Hidrosfera(72,2),null,null),new String[]{"H=","B-","rayleigh=","particles="});
        for(int j=0;j<3;j++) {
            var initial=List.of(base,modify(null,null,new Hidrosfera(0,0),null,null),modify(null,null,new Hidrosfera(100,0),null,null)).get(j);
            String label="one_step_small_population_"+j;
            for(long pop:new long[]{1,2,3,4,5,41,42}) {
                long stepId=experiment("step_rounding");
                var e=new EstadoPlanetario(initial.atmosfera(),initial.clima(),initial.biosfera(),initial.hidrosfera(),new Civilizacao(pop),0);
                var after=ModeloPlanetario.avancar(e,1);snapshot(stepId,"step_rounding",label+"_pop"+pop,"evolve",0,1,e,after);
            }
        }
    }
    private void sensitivity(String label,EstadoPlanetario after,String[] rules) {
        long id=experiment("sensitivity");snapshot(id,"sensitivity",label,"controlled_difference",0,0,base,after);
        double[] b=values(base),a=values(after);
        for(String rule:rules) {
            String field=rule.substring(0,rule.length()-1);char direction=rule.charAt(rule.length()-1);int index=Arrays.asList(FIELDS).indexOf(field);
            if(index<0)throw new IllegalArgumentException(field);
            double delta=a[index]-b[index];boolean pass=direction=='='?Math.abs(delta)<=1e-9:direction=='+'?delta>0:delta<0;
            check(id,label,"sensitivity_"+field+direction,pass,delta,direction=='='?"zero":direction=='+'?"positive":"negative",1e-9);
        }
    }
    private void mementoPersistence(Map<String,EstadoPlanetario> scenarios) throws IOException {
        Path db=out.resolve("temporary-audit.db");
        if(Files.exists(db))throw new IOException("Refusing to reuse a database: "+db);
        for(var entry:scenarios.entrySet()) {
            long id=experiment("memento_persistence");String name=entry.getKey();var original=entry.getValue();
            var terra=new Terra(UUID.randomUUID(),"Audit "+name,original);var memento=terra.capturar("before event");
            var altered=new GuerraNuclearCreator().criar(1).aplicar(original);terra.atualizar(altered);terra.renomear("Changed");
            eq(id,name,"memento_immutable",memento.estado(),original);terra.restaurar(memento);eq(id,name,"restore_state",terra.estado(),original);eq(id,name,"restore_name",terra.nome(),"Audit "+name);
            var other=new TerraBuilder().build();
            reject("foreign_memento_"+name,IllegalArgumentException.class,()->other.restaurar(memento));
            try(var repo=new SqliteMundoRepository(db)) {
                repo.salvar(terra,null,memento);
                var event=new RegistroEvento(UUID.randomUUID(),terra.id(),Instant.parse("2026-11-15T12:00:00Z"),"Audit nuclear",1,0);
                terra.atualizar(altered);repo.salvar(terra,event,null);
                eq(id,name,"saved_snapshot_isolated",repo.snapshots(terra.id()).getFirst(),memento);
                terra.atualizar(original);
                reject("transaction_duplicate_event_"+name,PersistenciaException.class,()->repo.salvar(terra,event,null));
                eq(id,name,"rollback_state",repo.carregar(terra.id()).orElseThrow().estado(),altered);
                eq(id,name,"rollback_history",repo.historico(terra.id()).size(),1);
            }
            try(var repo=new SqliteMundoRepository(db)) {
                var loaded=repo.carregar(terra.id()).orElseThrow();eq(id,name,"reopen_state",loaded.estado(),altered);
                loaded.restaurar(repo.snapshots(terra.id()).getFirst());eq(id,name,"persisted_restore_exact",loaded.estado(),original);
                eq(id,name,"persisted_indicators",ModeloPlanetario.calcular(loaded.estado()),ModeloPlanetario.calcular(original));
                eq(id,name,"persisted_optics",AtmosphereOptics.from(loaded.estado()),AtmosphereOptics.from(original));
                snapshot(id,"memento_persistence",name,"persist_reopen_restore",1,0,original,loaded.estado());
                repo.salvar(loaded,null,null);
            }
        }
        Files.deleteIfExists(db);Files.deleteIfExists(out.resolve("temporary-audit.db-wal"));Files.deleteIfExists(out.resolve("temporary-audit.db-shm"));
    }
    private void finish() throws IOException {
        states.close();checks.close();factors.close();rejects.close();
        try(var summary=writer("checks-summary.csv")) {
            csv(summary,"rule","checks","failed","max_absolute_error_numeric");
            for(var e:ruleCounts.entrySet())csv(summary,e.getKey(),e.getValue()[0],e.getValue()[1],ruleMaxError.containsKey(e.getKey())?ruleMaxError.get(e.getKey()):"");
        }
        StringBuilder groups=new StringBuilder();for(var e:categories.entrySet()) {if(!groups.isEmpty())groups.append(',');groups.append('"').append(e.getKey()).append("\":").append(e.getValue());}
        String summary="{\n  \"completed_utc\": \""+Instant.now()+"\",\n  \"java_version\": \""+System.getProperty("java.version")+"\",\n  \"seed\": 20261115,\n  \"experiments\": "+experiments+",\n  \"snapshots\": "+snapshots+",\n  \"factor_rows\": "+factorRows+",\n  \"checks\": "+checkCount+",\n  \"failed_checks\": "+failures+",\n  \"rejection_experiments\": "+rejectionCount+",\n  \"categories\": {"+groups+"}\n}\n";
        Files.writeString(out.resolve("summary.json"),summary,StandardCharsets.UTF_8);System.out.print(summary);
    }
    public static void main(String[] args) throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Supply a fresh output directory below docs/auditoria.");
        Path path=Path.of(args[0]).toAbsolutePath().normalize();Path allowed=Path.of("docs/auditoria").toAbsolutePath().normalize();
        if(!path.startsWith(allowed)||path.equals(allowed))throw new IllegalArgumentException("Output must be a new subdirectory of docs/auditoria.");
        if(Files.exists(path.resolve("summary.json"))||Files.exists(path.resolve("snapshots.csv")))throw new IllegalArgumentException("Refusing to overwrite previous evidence.");
        var audit=new ExperimentalAudit(path);
        try {var scenarios=audit.scenarios();audit.sweeps();for(var e:scenarios.entrySet())audit.trajectory("scenario",e.getKey(),e.getValue());audit.events(scenarios);audit.rejections();audit.probes();audit.mementoPersistence(scenarios);}
        catch(Throwable ex) {audit.check(0,"audit","uncaught_exception",false,ex.toString(),"none",0);ex.printStackTrace();throw ex;}
        finally {audit.finish();}
        if(audit.failures>0)throw new IllegalStateException(audit.failures+" checks failed; inspect checks.csv");
    }
}
