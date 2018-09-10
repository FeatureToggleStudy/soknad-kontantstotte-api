const Familieforhold = (props) => {
    return (
        <div>
            <h3>{props.tekster['barnehageplass.harFaattPlassDato']}</h3>

            <OppsummeringsElement
                sporsmal={props.tekster['familieforhold.borForeldreneSammenMedBarnet.sporsmal']}
                svar={props.familieforhold.borForeldreneSammenMedBarnet}
            />

            {props.familieforhold.borForeldreneSammenMedBarnet === 'JA' &&
                <>
                    <OppsummeringsElement
                        sporsmal={props.tekster['oppsummering.familieforhold.annenForelderNavn.label']}
                        svar={props.familieforhold.annenForelderNavn}
                    />
                    <OppsummeringsElement
                    sporsmal={props.tekster['oppsummering.familieforhold.annenForelderFodselsnummer.label']}
                    svar={props.familieforhold.annenForelderFodselsnummer}
                    />
                    <OppsummeringsElement
                        sporsmal={props.tekster['familieforhold.annenForelderYrkesaktivINorgeEOSIMinstFemAar.sporsmal']}
                        svar={props.familieforhold.annenForelderYrkesaktivINorgeEOSIMinstFemAar}
                    />
                </>
            }

            {props.familieforhold.borForeldreneSammenMedBarnet === 'NEI' &&
                <OppsummeringsElement
                    sporsmal={props.tekster['familieforhold.erAvklartDeltBosted.sporsmal']}
                    svar={props.familieforhold.erAvklartDeltBosted}
                />
            }
        </div>
    );
};