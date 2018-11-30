package no.nav.kontantstotte.innsyn.service.rest;

import no.nav.kontantstotte.innsyn.domain.Barn;
import no.nav.kontantstotte.innsyn.domain.Person;
import no.nav.kontantstotte.innsyn.domain.FortroligAdresseException;
import no.nav.tps.innsyn.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.kontantstotte.innsyn.service.rest.InnsynConverter.relasjonDtoToBarn;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.rules.ExpectedException.none;

public class InnsynConverterTest {

    @Rule
    public ExpectedException exception = none();

    @Test
    public void at_kode6_kaster_exception_person() {
        exception.expect(FortroligAdresseException.class);

        PersoninfoDto dto = person1();
        dto.setSpesiellOpplysning(spesiellOpplysning("SPSF"));

        InnsynConverter.personinfoDtoToPerson.apply(dto);
    }

    @Test
    public void at_kode7_kaster_exception_person() {
        exception.expect(FortroligAdresseException.class);

        PersoninfoDto dto = person1();
        dto.setSpesiellOpplysning(spesiellOpplysning("SPFO"));

        InnsynConverter.personinfoDtoToPerson.apply(dto);
    }

    @Test
    public void at_annen_kode_enn_6_og_7_ikke_kaster_exception_person() {

        PersoninfoDto dto = person1();
        dto.setSpesiellOpplysning(spesiellOpplysning("UFB"));

        Person p = InnsynConverter.personinfoDtoToPerson.apply(dto);

        assertThat(p.getFornavn()).isEqualTo(dto.getNavn().getFornavn());
        assertThat(p.getMellomnavn()).isEqualTo(dto.getNavn().getMellomnavn());
        assertThat(p.getSlektsnavn()).isEqualTo(dto.getNavn().getSlektsnavn());

    }

    @Test
    public void at_personer_uten_spesiell_opplysning_konverteres() {

        PersoninfoDto dto = person1();
        Person p = InnsynConverter.personinfoDtoToPerson.apply(dto);

        assertThat(p.getFornavn()).isEqualTo(dto.getNavn().getFornavn());
        assertThat(p.getMellomnavn()).isEqualTo(dto.getNavn().getMellomnavn());
        assertThat(p.getSlektsnavn()).isEqualTo(dto.getNavn().getSlektsnavn());
    }

    @Test
    public void at_barn_konverteres() {

        List<RelasjonDto> dtoList = relasjonList1();
        List<Barn> barnList = dtoList.stream()
                .map(dto -> relasjonDtoToBarn.apply(dto))
                .collect(Collectors.toList());
        for(int i = 0; i < dtoList.size(); i++){
            assertThat(barnList.get(i).getFulltnavn()).isEqualTo(dtoList.get(i).getForkortetNavn());
            assertThat(barnList.get(i).getFodselsdato()).isEqualTo(dtoList.get(i).getFoedselsdato());
        }
    }

    private KodeMedDatoOgKildeDto spesiellOpplysning(String kodeVerdi) {
        KodeMedDatoOgKildeDto dto = new KodeMedDatoOgKildeDto();

        KodeDto kode = new KodeDto();
        kode.setVerdi(kodeVerdi);
        dto.setKode(kode);

        return dto;
    }

    private PersoninfoDto person1() {
        PersoninfoDto dto = new PersoninfoDto();
        NavnDto navn = new NavnDto();
        navn.setFornavn("fornavn1");
        navn.setSlektsnavn("etternavn1");
        dto.setNavn(navn);
        return dto;
    }

    private RelasjonDto relasjon1() {
        RelasjonDto dto = new RelasjonDto();
        dto.setIdent("11111111111");
        dto.setForkortetNavn("fornavn1 etternavn1");
        dto.setFoedselsdato("11.11.1111");
        return dto;
    }

    private RelasjonDto relasjon2() {
        RelasjonDto dto = new RelasjonDto();
        dto.setIdent("22222222222");
        dto.setForkortetNavn("fornavn2 etternavn2");
        dto.setFoedselsdato("22.22.2222");
        return dto;
    }

    private List<RelasjonDto> relasjonList1() {
        return new ArrayList<RelasjonDto>() {{
            add(relasjon1());
            add(relasjon2());
        }};
    }
}
