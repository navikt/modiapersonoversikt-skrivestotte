import React from 'react';
import Header from "./components/header/header";
import RedigerVisning from "./sider/redigering/visning";
import StatistikkVisning from "./sider/statistikk/visning";
import useRouting, {Page} from "./hooks/use-routing";
import './application.less';

interface Props {
    renderHead: boolean;
}

function Application(props: Props) {
    const page = useRouting();
    const visning = page === Page.REDIGER ? RedigerVisning : StatistikkVisning;
    return (
        <div className="application">
            {props.renderHead && <Header/>}
            <div className={'application__content ' + `${page.slice(1)}-page`}>
                { React.createElement(visning) }
            </div>
        </div>
    );
}

export default Application;
