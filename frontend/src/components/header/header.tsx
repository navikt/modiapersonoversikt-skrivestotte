import React from 'react';
import { Element } from 'nav-frontend-typografi'
import { ReactComponent as Logo } from './nav-logo.svg';
import './header.less';

function Header() {
  return (
    <div className="application__header header">
      <Logo className="header__logo" />
      <Element tag="h1" className="header__appnavn">Modiapersonoversikt - Skrivestøtte admin</Element>
    </div>
  );
}

export default Header;
