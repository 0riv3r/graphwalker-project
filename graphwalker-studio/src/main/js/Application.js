import React, { Component } from 'react';
import { connect } from "react-redux";
import Container from "./components/container";
import StatusBar from "./components/statusbar";
import SideMenu from "./components/sidemenu";
import Editor from "./components/editor";
import ConfigPanel from "./components/configpanel";
import { Divider } from "@blueprintjs/core";
import Banner from "./graphwalker.inline.svg";
import SplitterLayout from 'react-splitter-layout';
import './style.css';
import PanelGroup from "react-panelgroup";

class Application extends Component {

  render() {
    if (this.props.showBanner) {
      return (
        <Container column>
          <Container>
            <SideMenu/>
            <Banner className="banner"/>
          </Container>
        </Container>
      )
    } else {
      return (
        <Container column>
          <Container>
            <SideMenu/>
            <PanelGroup borderColor="grey" panelWidths={[
              { width: 400, resize: "dynamic" },
              { resize: "stretch" }
            ]}>
              <ConfigPanel/>
              <Editor/>
            </PanelGroup>
          </Container>
          <StatusBar/>
        </Container>
      )
    }
  }
}

const mapStateToProps = ({ test: { models }}) => {
  return {
    showBanner: models.length === 0
  }
};

export default connect(mapStateToProps)(Application);
