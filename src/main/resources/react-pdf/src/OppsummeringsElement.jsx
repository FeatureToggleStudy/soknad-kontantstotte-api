const style = {
    tekst: {
        whiteSpace: 'pre-wrap',
    }
};

const OppsummeringsElement = (props) => {
    return (
      <div>
          { props.sporsmal &&
            <h4>{props.sporsmal}</h4>
          }
          <p style={style.tekst}>{props.svar}</p>
      </div>
    );
};