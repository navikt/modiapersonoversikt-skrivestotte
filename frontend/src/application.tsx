import React from 'react';
import classNames from "classnames";
import Header from "./components/header/header";
import RedigerVisning from "./sider/redigering/visning";
import StatistikkVisning from "./sider/statistikk/visning";
import useRouting, {Page} from "./hooks/use-routing";
import './application.less';

interface Props {
    renderHead: boolean;
}

function contentCls(page: Page): string {
    return classNames('application__content', `${page.slice(1)}-page`);
}

function Application(props: Props) {
    const page = useRouting();
    const visning = page === Page.REDIGER ? RedigerVisning : StatistikkVisning;
    return (
        <div className="application">
            {props.renderHead && <Header/>}
            <div className={contentCls(page)}>
                { React.createElement(visning) }
            </div>
        </div>
    );
}

export default Application;
